# Integrating Real iperf3 3.21 as a Native Library Across Android, iOS, and Desktop

**Status:** Planning — architecture decided, implementation not started.
**Companion documents:** `NATIVE_FORMATTING_JUSTIFICATION.md` (the printf integration this design deliberately mirrors), `PRINTF_FORMATTING_CASE_STUDY.md` (why that mirroring matters — see §7 below).

## 1. Current State

`IperfRunner` (`shared/src/commonMain/.../runner/IperfRunner.kt`) is a stub: `runIperfLive()` replays canned lines from `sampleOutputData.kt` on a delay loop, `forceStop()` and `setTempDir()` are no-ops that just `println`. `Iperf3Executor` — the layer above it — is otherwise fully built out: it constructs a real iperf3-style argv array from `UiInputData` (`--client`, `--port`, `--reverse`, `--parallel`, `--connect-timeout`, `--time`, `--omit`, etc.), drives the test through a `CoroutineScope(Dispatchers.IO)`, and threads output through `IperfCallback` (`onOutput(line)` / `onError(error)` / `onComplete()`) into `Iperf3OutputMonitor.processLine()`, which parses each line into an `Iperf3RunningState` (interval number, bandwidth, running min/max/avg/median/stddev, stall detection, etc.) that the Compose UI observes. `IperfCallback`'s `onOutput` signature (a single `String` per call) and `Iperf3OutputMonitor.processLine`'s job (parse one line into one state update) both assume iperf3's traditional human-readable columnar output — the exact format `native_format.h`'s `format_string()` was built to reproduce.

**The task:** replace `IperfRunner`'s simulation with real iperf3 3.21, without changing `Iperf3Executor`'s or the UI's contract any more than necessary.

## 2. Why This Is a Different Class of Problem Than `format_string()`

The printf integration (`NATIVE_FORMATTING_JUSTIFICATION.md`) had an escape hatch on every platform: JNI, cinterop, or a same-language stdlib call, freely mixed per platform, because `formatString()` is a single stateless function call with a bounded argument list. iperf3 breaks both of those properties:

1. **No subprocess fallback on mobile.** iOS sandboxing forbids `fork`/`exec` of a bundled executable outright — there is no way to ship an `iperf3` binary and spawn it as a child process on iOS, full stop. Modern Android (W^X enforcement since Android 10) similarly blocks executing an arbitrary bundled ELF binary that wasn't loaded as a proper `.so` via the standard native-library loader. **This means the mobile targets have exactly one viable path: link iperf3 in-process as a library and call its C API directly — there is no simpler fallback to retreat to if that gets hard**, unlike printf where `NSString.stringWithFormat:` was always sitting there as an (ultimately broken, see the case study) escape hatch. Desktop is the only target where a real subprocess would trivially work — see §3's decision to not special-case it anyway.
2. **Stateful and long-running, not a single call.** `format_string()` returns a value once, synchronously. iperf3 runs a test for seconds to hours, and this app needs live progress during that time — one or more values need to flow *out* of the native side, continuously, from a background thread, into a Kotlin callback, for the whole run.
3. **Needs real cancellation.** `forceStop()` has to actually interrupt an in-flight, blocking native call. printf never had a "stop halfway through formatting" concept.
4. **The vendored source is enormous by comparison.** `native_format.h` was ~160 lines this project wrote from scratch. iperf3 3.21 is a real, independently-maintained C codebase (`esnet/iperf` on GitHub) with its own build system, platform `#ifdef`s, and release cadence — this project is *consuming* it, not authoring it.

Despite all of that, §5 below shows the actual shape of the fix is closer to the printf work than this list makes it sound — see §7.

## 3. Decisions

### 3.1 In-process library linking on every target — not subprocess-on-desktop

Even though desktop could trivially spawn a real `iperf3` binary via `ProcessBuilder`, the integration uses `libiperf` in-process on **all three** platforms (JNI on Android and desktop JVM, cinterop on iOS) — the same target/mechanism split as `format_string()`. Rationale: iOS and Android have no choice (§2.1), and giving desktop a structurally different code path (subprocess + stdout scraping) means maintaining two independent implementations of "run an iperf3 test and report progress" that can drift, rather than one. The cost is more upfront work on desktop than strictly necessary; the benefit is one code path, one set of bugs, one thing to test.

### 3.2 `--json-stream` for live output, not text-column scraping

iperf3 3.17+ supports `-J`/`--json-stream` mode: one JSON object per line, emitted as the test progresses, specifically designed for programmatic consumption — as opposed to the traditional human-readable columnar output that `native_format.h` exists to reproduce and that `Iperf3OutputMonitor` currently regex/column-parses. The integration uses JSON-stream output as the actual data contract between native iperf3 and Kotlin.

**This has a real, direct consequence for the printf work just finished:** `format_string()` and its fixed-width iperf3-style column formatting (`"%-12.12s %4.4s %-9.9s %10.10s"`, as seen in `Iperf3OutputMonitor.kt` and its test suite) is now purely a **display concern** — reproducing iperf3's classic human-readable look for whatever renders the running log to the user — not a *parsing* concern. `Iperf3OutputMonitor.processLine()`'s job changes from "regex a fixed-width text line" to "parse one JSON object per interval," which is a substantially simpler and more robust parser, and removes an entire class of fragility (column width assumptions breaking on unusual hostnames/units) that the current text-based parser has to handle. `format_string()` itself is not wasted work — it's still exactly what's needed to render iperf3-style output for the user — but it is no longer on the critical path for *understanding* iperf3's output, only for *displaying* it.

### 3.3 iperf3 source as a git submodule

`esnet/iperf` is added as a git submodule (pinned to the `3.21` tag) rather than a vendored copy, so upstream security fixes/version bumps are a submodule bump + rebuild, not a manual re-diff against a committed copy. Cost: contributors need `git submodule update --init` after cloning (to be called out in `AGENTS.md`/`README.md` once implemented).

## 4. Proposed Architecture

Same three-interop-mechanism shape as `native_format.h`, but wrapping `libiperf`'s existing C API (`iperf_api.h`) instead of a from-scratch C function:

```
                         third_party/iperf (git submodule, esnet/iperf @ 3.21)
                                        |
                         native/iperf3_bridge.{h,c}  (this project's shim —
                         owns test lifecycle, callback marshaling, cancellation;
                         NOT a reimplementation of iperf3 itself)
                    /                   |                    \
           JNI (Android)         JNI (desktop)          cinterop (iOS)
                |                       |                       |
      :iperf3native module      compileIperf3Jvm task    iperf3_bridge.def
      (new module, CMake/NDK    (new Gradle task,         (cinterop against
       building libiperf +       host toolchain)           the shim + a real,
       the JNI shim into one         |                      compiled static
       .so — same AGP-9            jvmMain                  lib this time —
       workaround pattern as     IperfNativeBridge.kt        unlike
       :nativeformat)                  |                      format_string(),
           |                      jvmMain                     this can't be
      androidMain               IperfRunner.kt (actual)      static inline;
      IperfNativeBridge.kt                                    see §6.1)
           |                                                       |
      androidMain                                              iosMain
      IperfRunner.kt (actual)                                  IperfNativeBridge.kt
                                                                     |
                                                                 iosMain
                                                                 IperfRunner.kt (actual)
```

`iperf3_bridge.c` is this project's own code, and its job is narrow and specific — it is *not* a reimplementation of any iperf3 logic, only the glue needed to make `libiperf`'s blocking, callback-poor C API usable from a Kotlin `expect`/`actual` boundary:

- Own a `struct iperf_test*` per running test, built from the same argv-shaped input `Iperf3Executor` already constructs.
- Redirect/hook wherever `libiperf` writes its `--json-stream` output (see §6.2 — the exact mechanism depends on what `libiperf` 3.21 actually exposes for this, to be confirmed against the vendored source directly rather than assumed from memory of older iperf3 versions) into a line-oriented C callback this shim owns.
- Bridge that C callback across the interop boundary to Kotlin's `IperfCallback` — via the same "no generic vararg, keep the payload to a fixed, simple shape" principle as `native_format.h` (§7): each JSON-stream line is just a `const char*`, a single fixed-type value, so — unlike printf's argument-list problem — there is no tagged-union marshaling needed here at all. This part is *easier* than printf, not harder.
- Run the test on a background thread (all three platforms), and provide a real cancellation entry point — most likely translating `forceStop()` into whatever `libiperf` uses internally for graceful shutdown (iperf3's CLI handles `SIGINT` by setting a "done" flag the test loop checks; a library caller running in-process should be able to trigger the equivalent flag directly without needing an actual OS signal — to be confirmed against `iperf_api.h`).

`IperfRunner`'s `expect`/`actual` split stays exactly where it already is in `commonMain`; only the `actual` bodies change, from the current simulation to a call into the corresponding platform's `IperfNativeBridge`.

## 5. Platform-Specific Notes

- **Android:** New `:iperf3native` module, same structural reason as `:nativeformat` — AGP 9's `com.android.kotlin.multiplatform.library` plugin still can't run `externalNativeBuild`/CMake, so this is another standalone classic `com.android.library` module. Unlike `:nativeformat`, this one's CMake build is nontrivial on its own terms: it has to build `libiperf` itself (with its own `configure`/autotools or CMake, whichever iperf3 3.21 actually ships — to confirm once the submodule is added) for all 4 ABIs, not just compile one small shim file. Requires `INTERNET` permission (already presumably present, given this is a network-testing app) and needs checking whether any iperf3 features used depend on capabilities unavailable in Android's app sandbox (e.g., certain socket options).
- **iOS:** Needs an `NSLocalNetworkUsageDescription` entry in `Info.plist` and will hit the same Local Network privacy permission prompt documented in `LMSTUDIO_MCP_CASE_STUDY.md` §5 (that case study was about a different app, but the underlying OS mechanism — a distinct local-network permission separate from general internet access — applies here directly, and is actually *more* likely to bite for an app whose entire purpose is opening raw sockets to arbitrary hosts). Also: per §4's diagram, `iperf3_bridge` cannot be `static inline` the way `format_string()` was, because it needs to link against the compiled `libiperf` static library, not just be textually included — this brings back exactly the "build a static lib per iOS arch and link it via cinterop" step that `native_format.h`'s header-only design let this project skip entirely (see `NATIVE_FORMATTING_JUSTIFICATION.md`'s iOS row). That step needs real design work: which Kotlin/Native cinterop mechanism (`staticLibraries`/`libraryPaths` in the `.def` file vs. a Gradle task invoking `xcrun`/`clang` directly per iOS arch, mirroring `iosApp`'s existing Xcode-managed build) is used to actually produce that library before cinterop links against it.
- **Desktop (JVM):** Closest to the Android JNI path structurally (§3.1), but the *build* is closer to `:nativeformat`'s `compileNativeFormatJvm` task — compiled at Gradle build time against the host toolchain for the host OS/arch, bundled as a resource, `System.load()`ed at runtime — except this time the task also has to build `libiperf` itself for the host, not just compile one shim file against `jni.h`.

## 6. Open Technical Questions (to resolve during implementation, not assumed here)

1. **What does `libiperf` 3.21 actually expose for JSON-stream output redirection?** Needs reading the actual vendored `iperf_api.h`/`iperf.c` once the submodule is added — do not assume behavior from memory of older iperf3 versions. iperf3's JSON output has changed across versions (streaming JSON output, in particular, was added later than the base JSON `-J` mode and matured over several releases).
2. **iperf3's own build system vs. this project's per-platform build.** iperf3 upstream builds via autotools (`./configure && make`) by default; some forks/ports provide CMake. Need to determine, once the submodule is in, whether to (a) drive iperf3's own build system from each platform's native-build step, or (b) write CMake-based (Android/iOS) and Makefile/host-toolchain-based (desktop) builds that compile iperf3's sources directly, bypassing its own build system. Same category of build-system-archaeology work that `NATIVE_FORMATTING_JUSTIFICATION.md` needed for AGP 9's KMP plugin limitation (§7).
3. **Reentrancy/thread-safety of `struct iperf_test`.** Confirm iperf3 3.21 supports running one test at a time cleanly from a library caller (this app never needs concurrent tests, but needs to confirm there's no global/static state left over between runs that would corrupt a second sequential test).
4. **Cancellation mechanism specifics** — see §4's note on `forceStop()`.
5. **Android/iOS permission and entitlement requirements** for the specific socket operations iperf3 performs (raw TCP/UDP client and server modes, `SO_REUSEADDR`, etc.).

## 7. Why This Design Deliberately Mirrors `native_format.h`

`PRINTF_FORMATTING_CASE_STUDY.md` documents, with direct evidence, a failure pattern from three earlier local-model attempts at the printf task: each one either avoided a real interop boundary at the cost of reimplementing logic that already existed elsewhere (Attempt 1), or hit the boundary and quietly narrowed the problem's contract into something that merely compiled rather than surfacing that the correct approach couldn't be executed as planned (Attempt 3 — the `NSArray`-as-vararg bug, undetected until it hung). The generalizable lesson from that case study — confirmed again by §2 of this document — is that **the hard part of "just call this native thing from Kotlin" is almost never the domain logic; it's getting a value (or a stream of values) across a real FFI/interop boundary without silently violating that boundary's actual contract.** This design responds to that lesson directly: keep the interop payload as simple as it can possibly be (a `const char*` per JSON line, not a generic argument list — see §4), reuse the same three concrete mechanisms already proven end-to-end for `format_string()` (JNI ×2, cinterop ×1), and resolve every genuinely open technical question (§6) against the real vendored source once it's available, rather than against assumption.
