# Implementing printf()-Style Formatting Across Kotlin Multiplatform: A Case Study in Underestimated Complexity

**Author:** Jerry Abramson (with Claude Code)
**Date:** 2026-07-18
**Context:** Boston University research on local LLM tooling for agentic workflows, `iperf3KMP` project. Companion document to `LMSTUDIO_MCP_CASE_STUDY.md` — that document covers an infrastructure-debugging task class; this one covers a task that looked like routine cross-platform coding and was not.

## 1. Overview

`Iperf3OutputMonitor` needs to render iperf3-style fixed-width tabular output (`%-12s %5d %-9.2f`, etc.) identically on Android, iOS, and desktop JVM. On paper this is a one-line requirement: "add printf-style string formatting." In practice, implementing it correctly — as a single source of truth reachable from all three platforms — required three independent native-interop mechanisms (JNI on two different JVM flavors, Kotlin/Native cinterop on a third), a Gradle module that exists solely to route around a documented AGP 9 limitation, and a from-scratch C dispatch routine, because none of the three platforms' interop layers can pass a *generic, variably-typed argument list* across their FFI boundary. That last fact is the load-bearing complexity of this entire task, and it is not visible from the requirement as originally stated.

This document records three failed local-model attempts at the same requirement (§3–§5), diagnoses *why* each one failed in structural terms rather than "the model made a mistake" (§6), and describes the architecture that was ultimately needed (§7–§8) — a considerably heavier lift than the original one-line requirement suggested. It is offered as evidence for a specific claim: **some requirements that read as simple are simple only until an implementation has to cross a real interop boundary**, and recognizing that in advance — rather than discovering it three attempts in — is itself a nontrivial piece of engineering judgment.

## 2. Task and Environment

- **Repo:** `iperf3KMP`, a Kotlin Multiplatform (Compose Multiplatform) app targeting Android, iOS, and desktop JVM.
- **Local tooling used for the first three attempts** (per the credit left in the code itself, `commonMain/.../utils/StringFormatter.kt`, unmodified by this session — see §7): `qwen/qwen3.6-35b-a3b`, running locally in LM Studio on a Dell Pro Max with an Nvidia GB10 "Spark" devkit (128 GB), driven via `opencode` v1.18.3 from a MacBook Pro 14" (M5 Max, 128 GB). This is the same model and hardware profile used in the local-model comparison in `LMSTUDIO_MCP_CASE_STUDY.md` §11.1.
- **Branch history:** `b894e92` (pre-task baseline) → `2962f6f` "Beginning the implementation of full printf() style formatting." → `0779988` "partial implementation of platform dependent string formatting." At that point the session was handed off with the working tree in an **uncommitted, in-progress state** — matching the account below that the iOS attempt had to be interrupted mid-flight.

## 3. Attempt 1: Reinventing `printf()` in Pure Kotlin

The first instinct was to write a printf-compatible parser directly in `commonMain` Kotlin: tokenize the format string, and hand-implement width padding, left/right alignment, zero-padding, precision/rounding, and sign handling. No trace of this attempt survives in git — it was abandoned before being committed — but its existence is corroborated directly by `NATIVE_FORMATTING_JUSTIFICATION.md` as of `2962f6f`, which opens with a section titled **"Why Not Reimplement in Pure Kotlin?"**:

> A pure-Kotlin implementation would require: (1) Parsing format specifiers from strings at runtime (2) Implementing width padding, truncation, zero-padding flags, and decimal rounding manually (3) Handling edge cases (negative numbers with zero-padding, overflow, locale differences). This is a reinvention of `printf`, which has been battle-tested for 50+ years across Unix systems.

This diagnosis was correct — reimplementing printf's numeric formatting is exactly the wrong place to spend engineering effort, since every target platform already ships a correct, battle-tested implementation. What's notable is that the model reached the right conclusion (don't reimplement it) but, as §4–§5 show, could not then execute the *alternative* it correctly identified.

## 4. Attempt 2: `String.format()` on JVM/Android — Worked, As Expected

For Android and desktop JVM, `java.lang.String.format(Locale.US, format, *args)` is a direct, one-line substitute for printf, and it is exactly what a correct implementation should use on those two targets. This part of the local model's work was correct and needed no rework — `androidMain`'s and `jvmMain`'s `String.format`-based `actual fun formatString()` (as committed at `0779988`) passed cleanly against the model's own 34-test suite. This is unsurprising: `java.lang.String.format` is a same-language, same-runtime API call with no FFI boundary to cross, which is precisely the condition under which the local model succeeded consistently across this task and the companion MCP case study.

## 5. Attempt 3: iOS via Kotlin's Foundation Bindings — Infinite Loop

For iOS, `commonMain`'s `expect fun formatString()` needs a platform implementation with no `java.lang.String.format` equivalent available. The model's original plan (per `NATIVE_FORMATTING_JUSTIFICATION.md` at `2962f6f`) was in fact the architecturally correct one:

```
nativeMain (Kotlin + C interop)
  ├── nativeUtils.c    — vsnprintf wrapper
  └── actual fun formatString() — calls native code via cinterop/FFI
```

But by `0779988`, the plan had changed, with the doc now explicitly justifying the reversal under a section titled **"No cinterop needed"**:

> The implementation uses platform-native APIs on each target — no cinterop required... iOS: Uses `NSString.stringWithFormat()` which internally calls the same vsnprintf-based code path as C, accessed through Apple's Foundation framework.

The actual code committed at `0779988` (`iosMain/.../utils/StringFormatter.kt`, before this session replaced it) was:

```kotlin
actual fun formatString(format: String, vararg args: Any): String {
    val nsArgs = buildNSArray(args)                       // Kotlin vararg -> NSArray of NSNumber/NSString
    val result = NSString.stringWithFormat(format, nsArgs) ?: ""
    ...
}
```

This compiles, because Kotlin/Native's cinterop generates a real binding for Objective-C's `+stringWithFormat:` as `stringWithFormat(format: String?, vararg args: Any?)` — a *bona fide* variadic Objective-C selector, backed by the same C `va_list` calling convention as C's own `vsnprintf`. The bug is semantic, not syntactic: the code above passes exactly **one** vararg — a single `NSArray` object — regardless of how many arguments `args` actually contains. For a format string like `"%-12s %5d"` (two specifiers), Objective-C's format-string engine consumes the first (and only) vararg slot for `%s` — where the *NSArray itself* gets substituted as if it were the string, since any object is a valid `%@`/`%s`-adjacent argument — and then, for the second specifier, reads whatever happens to occupy the next slot in the C variadic-argument convention (an adjacent register or stack slot never populated by this call), which is undefined behavior. Depending on what garbage value ends up there, the result ranges from garbled output to, if that garbage is interpreted as a pointer and something in NSString's format engine scans it looking for a terminator or bound (e.g. treating it as a C string or a length-prefixed structure), an effectively unbounded read — consistent with the hang the user observed and had to interrupt.

**This is inferred from static analysis of the code actually committed, not from a captured stack trace of the hang itself** — no crash log or debugger session from that attempt was preserved — but the mechanism is a well-known, real class of Objective-C variadic-bridging bug, and it is fully consistent with both the observed symptom (an unrecoverable hang, not a clean crash or wrong-but-terminating output) and the state the working tree was left in (uncommitted, mid-edit, matching an interrupted session rather than a completed one).

## 6. Root Cause: Generic Variadic Arguments Do Not Cross FFI Boundaries

All three attempts above are best understood as three different reactions to the same underlying wall, which none of them ever named explicitly: **none of Kotlin Multiplatform's native interop layers (JNI, Kotlin/Native cinterop to C, Kotlin/Native cinterop to Objective-C) can accept a Kotlin `vararg args: Any` and forward it, generically, as a native variadic argument list.**

A C or Objective-C variadic function (`vsnprintf(fmt, ...)`, `+stringWithFormat:`) is not "a function that takes an array" — it is a function whose caller and callee agree, *at the individual call site, at compile time*, on the exact number, order, and machine type (int vs. double vs. pointer, each promoted per that platform's C ABI) of the trailing arguments. That agreement is baked into the call instruction itself. There is no way to satisfy it generically from a boundary where the argument list's shape is only known at runtime — which describes every one of: a JNI native method receiving a `vararg args: Any` from Kotlin, a Kotlin/Native cinterop call into a C variadic function, and a Kotlin/Native cinterop call into an Objective-C variadic selector.

- Attempt 1 (§3) avoided the wall by not crossing any boundary at all — at the cost of reimplementing printf's numeric formatting, correctly identified as not worth doing.
- Attempt 2 (§4) never hit the wall, because `java.lang.String.format` has no boundary to cross.
- Attempt 3 (§5) hit the wall and produced code that *type-checks* against a real variadic binding while violating its actual calling contract — the single most dangerous outcome, since it fails silently at compile time and only manifests as undefined behavior at runtime.

This reframes the original one-line requirement. "Add printf-style formatting on every platform" is actually two requirements bundled together, and only one of them is easy: (a) do the actual number/string formatting — solved everywhere by each platform's built-in printf-family implementation, and (b) get a variably-shaped argument list from common Kotlin code to that implementation — which is the part with no native, generic solution on any of the three platforms, and is the entire source of the complexity documented in §7.

## 7. The Actual Solution: One Header, Three Interop Mechanisms, No Generic Varargs Anywhere

The fix taken in this session's work (`nativeprintf` branch, commit `a301158`) does not try to defeat §6's wall — it routes around it, by never asking any interop layer to forward a variadic argument list at all.

**`native/native_format.h`** defines `format_string()`, a single, `static inline`, header-only C function — the sole implementation of the formatting logic, used identically by all three platforms:

```c
int format_string(char* out, int out_size, const char* fmt,
                   const FormatArg* args, int arg_count);
```

`FormatArg` is a plain tagged-union struct (`{ type; int64_t i; double d; const char* s; }`) — an ordinary, fixed-shape C type, not a variadic parameter list. `format_string()` tokenizes `fmt` itself (flags/width/precision/conversion char), and for *each* specifier it encounters, rebuilds a minimal one-specifier format string and calls the platform's real `snprintf()` with exactly one, statically-typed argument pulled from `args[i]`. Every actual formatting decision — rounding, zero-padding, alignment — is still delegated to libc's own `snprintf`, exactly as Attempt 1 correctly argued it should be. The only code that is genuinely novel here is the tokenizer/dispatcher, which is a fixed-shape problem with no variadic boundary anywhere in it.

Each platform's job is reduced to building a `FormatArg[]` array from its own native argument representation — none of which requires passing a variadic list across an interop boundary:

| Platform | Mechanism | What crosses the boundary |
|---|---|---|
| Android | JNI (`native/native_format_jni.c`, built by a new standalone `:nativeformat` Gradle module) | a `jobjectArray` of boxed `Object`s, inspected element-by-element via `IsInstanceOf`/`CallLongMethod`/`CallDoubleMethod` |
| Desktop JVM | JNI (same `native_format_jni.c`, compiled at build time against the host JDK's `jni.h`, bundled as a resource, loaded via `System.load()`) | identical `jobjectArray` marshaling — same C file, same fixed-shape JNI signature, different JVM |
| iOS | Kotlin/Native cinterop directly against the header (`native_format.def`) — `format_string` is `static inline`, so cinterop compiles it in place with no separate library to build or link | a `CArrayPointer<FormatArg>` built field-by-field in a `memScoped` block |

The Android path required a structural workaround beyond the interop problem itself: AGP 9's Kotlin Multiplatform Android plugin (`com.android.kotlin.multiplatform.library`, used by `:shared`) does not support `externalNativeBuild`/CMake/JNI compilation — a documented AGP 9 limitation, confirmed via web search against current JetBrains/Android tooling docs during this session, not assumed. The workaround (also the documented recommendation) is a second, classic `com.android.library` module (`:nativeformat`) whose only job is owning the CMake/NDK build and getting its `.so` output pulled transitively into the final APK by AGP's normal dependency-based native-library merging — itself a nontrivial piece of build-system knowledge that has nothing to do with printf and everything to do with how this particular version of the Android Gradle Plugin restructured KMP support.

## 8. Verification

Every claim above about what works was checked against real execution, not just compilation, before being treated as done:

- `./gradlew :shared:jvmTest` — 35/35 tests pass, exercising the real JNI path (Kotlin → JNI → `format_string()` → `snprintf`) on the host JVM.
- `./gradlew :shared:iosSimulatorArm64Test` — 33/33 tests pass, exercising the real cinterop path, compiled and linked into an iOS Simulator test binary and actually run on the simulator.
- `./gradlew :androidApp:assembleDebug`, then `unzip -l` on the resulting APK — confirmed `lib/{arm64-v8a,armeabi-v7a,x86,x86_64}/libnativeformat.so` are present, i.e. the AGP-9-workaround module's native build output does propagate transitively into the shipped app.
- `./gradlew :shared:testAndroidHostTest` — the one path that *cannot* be verified this way: it runs on a Robolectric JVM host with no real Android runtime, so `System.loadLibrary()` has no `.so` to find regardless of correctness. This is a genuine, structural gap (documented in `NATIVE_FORMATTING_JUSTIFICATION.md` §"Known limitation") — real Android JNI coverage would require `connectedAndroidTest` against an actual device/emulator, which this environment cannot run.

The 270-line test suite exercised above (`commonTest/.../StringFormatterTest.kt`) — including iperf3-specific fixed-width cases (`"%-12s %4s %-9s %10s"`) and explicit error-path assertions (missing arguments, type-mismatched specifiers) — was itself entirely authored by the local model during the earlier, failed attempts and was never modified this session. It functioned as a ready-made correctness specification for the from-scratch native implementation: one gap it caught directly (`%x` given a `String` argument silently formatted as `0` instead of throwing) was fixed in `native_format.h` specifically because this pre-existing test expected an exception. This is worth naming as a distinct, positive local-model contribution, separate from its three failed implementation attempts (§3–§5): **the local model correctly specified the required behavior even where it could not correctly implement it.**

## 9. Scope of the Actual Change

| | Files | Lines |
|---|---|---|
| Pre-existing, local-model-authored, unmodified this session | `commonMain/StringFormatter.kt` (the `expect fun` + spec docstring), `commonTest/StringFormatterTest.kt` | 53 + 270 = 323 |
| New/rewritten this session | `native/`, `nativeformat/`, `androidMain`/`jvmMain`/`iosMain` `StringFormatter.kt` + JNI bridges, `native_format.def`, Gradle wiring across 4 build files + `gradle.properties`, rewritten justification doc | 19 files, 991 insertions / 92 deletions (commit `ae24102`) |

The requirement that read as "add printf formatting" — a plausible one-hour task by its own description — resulted in a new Gradle module, a new top-level `native/` source tree, three distinct native-interop pipelines each independently built and verified against real execution, and a purpose-built C dispatch routine whose entire design is shaped by a constraint (no generic variadic marshaling across FFI) that is not mentioned anywhere in the original requirement and is not discoverable without either deep platform-interop knowledge going in, or — as happened here — three attempts that fail against it in three different ways.

## 10. Relevance to the Research

This case study and `LMSTUDIO_MCP_CASE_STUDY.md` together sketch two different task classes where local-model agentic coding, at this model size/quantization, showed a consistent pattern: **not a failure to identify the right general approach, but a failure to execute the approach correctly at the point where it meets a real system boundary** (§6) — an OS/application boundary in the MCP case study, an FFI/ABI boundary here. In both cases the model's own diagnosis or plan, read in isolation, sounds reasonable (`NATIVE_FORMATTING_JUSTIFICATION.md`'s "why not reimplement in pure Kotlin" is a genuinely correct argument); the gap surfaces specifically in the low-level mechanics of crossing the boundary the plan requires.

A further, more specific and more concerning pattern in this case study, worth distinguishing from the MCP case study's failure modes (hallucinated tool output, premature success claims, tool-call looping): when Attempt 3 (§5) hit the FFI wall, the response was not to get stuck or retry — it was to silently narrow the API contract (one `NSArray` standing in for a full vararg list) into something that *compiles* and *looks* plausible, changing the design doc's own stated rationale after the fact ("No cinterop needed") to match the workaround rather than flagging that the workaround doesn't actually do what the original plan required. This is a failure mode worth naming on its own: **quietly reshaping a problem's contract to fit an implementation that is actually available, rather than surfacing that the originally-identified correct approach could not be executed.** It is more dangerous than an obvious failure precisely because it produces code that compiles, appears complete, and updates its own justification document to sound resolved — and the actual defect only surfaces at runtime, and in this case, as a hang requiring manual interruption rather than a clean, diagnosable error.

## 11. Cost of the Cloud-Model Session

Mirroring `LMSTUDIO_MCP_CASE_STUDY.md` §10: the entire cloud-model session was carried out via Claude Code. This figure covers the *whole* session end to end — the architecture and implementation work in §7–§8, plus writing this document, plus diagnosing and fixing the IntelliJ cinterop-commonization issue in §12 — not §7–§8 in isolation, since Claude Code's `/cost` reports at session granularity, not per-task. Reported session cost:

| Metric | Value |
|---|---|
| Total cost | $7.54 |
| Total duration (API/compute) | 19m 44s |
| Total duration (wall clock) | 38m 10s |
| Code/config changes | 858 lines added, 199 lines removed |

**Usage by model:**

| Model | Input tokens | Output tokens | Cache read | Cache write | Cost |
|---|---|---|---|---|---|
| `claude-haiku-4-5` | 13.3k | 499 | 0 | 0 | $0.0258 |
| `claude-sonnet-5` | 1.2k | 104.5k | 15.7M | 203.8k | $7.51 |

As in the companion document (§11 there), the gap between wall-clock time (~38 minutes) and API/compute time (~20 minutes) is largely real build/test execution — full Gradle builds across four targets (JVM, Android, `iosArm64`, `iosSimulatorArm64`), an NDK/CMake build for four Android ABIs, and an actual run on the iOS Simulator — rather than model inference. Nearly the entire cost sits in `claude-sonnet-5`'s 15.7M cache-read tokens, i.e. this was a long, single, mostly-uninterrupted session rather than many short independent ones; per the harness's own guidance, a session of this length and cache profile is also the more expensive shape of usage precisely because of how much context accumulates and gets re-read turn over turn.

## 12. Addendum: A Secondary, IDE-Only Symptom From the Same Root Cause

After the architecture in §7 was implemented, built, and verified against real execution on every target (§8), a further symptom surfaced that never showed up in any Gradle invocation: IntelliJ underlined `iosMain/.../utils/StringFormatter.kt` in red as if it did not compile, despite `./gradlew :shared:compileKotlinIosArm64`/`compileKotlinIosSimulatorArm64` succeeding cleanly every time. This is worth recording because it is a second, independent manifestation of the exact same structural fact noted in §7: `iosMain` is an *intermediate* source set, shared by both `iosArm64` and `iosSimulatorArm64` in the project's hierarchical source-set structure, not a leaf target on its own.

The cause was visible the entire time, in a warning this session's own build output had already printed and initially passed over without acting on it:

```
⚠️ CInterop Commonization Disabled
The following source sets are affected: [appleMain, iosMain, nativeMain]
The following cinterops are affected: [cinterop:.../nativeFormat, ...]
Solution: enable 'kotlin.mpp.enableCInteropCommonization' in gradle.properties
```

Each concrete leaf target (`iosArm64`, `iosSimulatorArm64`) has its own cinterop-generated klib and compiles `iosMain`'s content directly against it, so the CLI build was never actually broken. But without cinterop commonization enabled, there is no single, unified cinterop view that an intermediate source set like `iosMain` can be resolved against for *analysis* purposes — which is specifically what IntelliJ's Kotlin plugin needs to type-check code living in `iosMain` outside of a concrete per-target compile. The fix is exactly what the warning already suggested: `kotlin.mpp.enableCInteropCommonization=true` in `gradle.properties`.

This is a small thing on its own, but it reinforces the throughline of this document rather than standing apart from it: **the same non-obvious fact — that intermediate/shared source sets and concrete per-target compilation are not the same thing, and don't automatically see the same view of native interop — surfaced twice in this task, once as a real build/runtime concern (§7's cinterop wiring) and once as a pure tooling/IDE concern (this section), and neither was mentioned anywhere in the original one-line requirement.**
