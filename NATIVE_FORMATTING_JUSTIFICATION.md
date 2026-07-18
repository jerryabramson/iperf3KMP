# Justification: Native C String Formatting for KMP iperf3 Project

## Problem Statement

The `Iperf3OutputMonitor` class parses raw iperf3 output lines that use fixed-width, column-aligned formatting (e.g., `%12.12s %4.4s %-9.9s %10.10s`). These format strings require:
- Field width specification with padding/truncation
- Left/right alignment control (`%-`)
- Decimal precision for floating-point values (`%.2f`, `%5.2d`)

Kotlin's common library does not provide `String.format()` or equivalent printf-style formatting — these APIs are JVM/Android-specific and unavailable on iOS/native targets.

## Why Not Reimplement in Pure Kotlin?

A pure-Kotlin implementation would require:
1. Parsing format specifiers from strings at runtime
2. Implementing width padding, truncation, zero-padding flags, and decimal rounding manually
3. Handling edge cases (negative numbers with zero-padding, overflow, locale differences)

This is a reinvention of `printf`, which has been battle-tested for 50+ years across Unix systems. The effort to implement correctly in Kotlin outweighs any benefit — it introduces bugs, maintenance burden, and deviates from the project's core research goals.

## Why Native C Is the Right Choice

### 1. The Project Already Uses Native C for iperf3
The application's primary purpose is running iperf3 network tests. This requires:
- Calling the iperf3 C library via FFI/cinterop
- Parsing iperf3's stdout output (which itself uses printf-style formatting)
- Managing native process lifecycle

Adding a small C utility for string formatting introduces no new dependencies or architectural complexity — it fits within the existing native interop pattern.

### 2. `vsnprintf` Is the Canonical Solution
The C standard library provides `vsnprintf()`, which:
- Handles all format specifiers (`%s`, `%d`, `%f`, width, precision, alignment)
- Is available on every target (JVM via JNI, iOS via native toolchain, Android NDK)
- Produces deterministic, locale-independent output when used with the "C" locale

### 3. Minimal Scope, Maximum Reliability
The C wrapper is a single function:

```c
int format_string(char* out, int outSize, const char* fmt, ...);
```

This delegates formatting to well-tested system code rather than introducing custom Kotlin logic. The wrapper handles buffer overflow protection via `vsnprintf`'s size limit.

## Implementation Architecture

### Final Design (Updated)

The implementation uses platform-native APIs on each target — no cinterop required:

```
commonMain (Kotlin)
  └── expect fun formatString(format: String, vararg args: Any): String

jvmMain / androidMain (Kotlin)
  └── actual fun formatString() — uses java.lang.String.format(Locale.US)

iosMain (Kotlin + Apple Foundation)
  └── actual fun formatString() — uses NSString.stringWithFormat()
```

This approach:
- **iOS**: Uses `NSString.stringWithFormat()` which internally calls the same vsnprintf-based code path as C, accessed through Apple's Foundation framework (always available on iOS)
- **JVM/Android**: Uses `java.lang.String.format(Locale.US)` for consistent decimal separators across platforms

### Files Created

| File | Purpose |
|------|---------|
| `commonMain/.../utils/StringFormatter.kt` | `expect` declarations with full documentation |
| `jvmMain/.../utils/StringFormatter.kt` | JVM implementation via `String.format(Locale.US)` |
| `androidMain/.../utils/StringFormatter.kt` | Android implementation (identical to JVM) |
| `iosMain/.../utils/StringFormatter.kt` | iOS implementation via `NSString.stringWithFormat()` |
| `commonTest/.../utils/StringFormatterTest.kt` | 34 comprehensive tests covering all format specifiers, edge cases, and iperf3-specific patterns |

### Supported Format Specifiers

All standard C printf specifiers are supported:
- `%s` — String (with width, left/right alignment)
- `%d`, `%i` — Integer with width, zero-padding, precision (`%05d`)
- `%f` — Floating-point with precision (`%.2f`, `%8.3f`)
- `%e`, `%g`, `%x`, `%o` — Scientific/decimal/hex/octal

Format flags: `-` (left-align), `0` (zero-pad), `+` (always show sign), `(space)` (space for positive)

### Key Design Decisions

1. **No cinterop needed** — iOS uses `NSString.stringWithFormat()` which provides identical printf-style formatting via Apple's Foundation framework
2. **US Locale on JVM/Android** — ensures consistent decimal separator (dot) across all platforms
3. **4096 character output limit** — prevents excessive memory usage for very long format strings
4. **Single function signature** — avoids vararg overload ambiguity issues

### Test Coverage

The test suite (`StringFormatterTest.kt`) covers:
- String padding and alignment (right/left, truncation)
- Integer formatting with width, zero-padding, negative values
- Floating-point formatting with precision, large values
- Mixed format specifiers matching iperf3 output patterns
- iperf3-specific formats: connection info lines, interval data, summary statistics
- Edge cases: empty strings, very long output, buffer limits
- Error handling: invalid format strings, missing arguments

All 34 tests pass on JVM. iOS and Android compile successfully.

## Impact on Research Goals

This change has no impact on the research objectives. It is purely an infrastructure improvement that:
- Reduces code complexity in the common parsing logic
- Eliminates a source of potential formatting bugs
- Aligns with the project's existing use of native C for iperf3 integration

The alternative — implementing printf-style formatting in Kotlin — would add ~200 lines of custom string manipulation code that duplicates functionality already available in the standard libraries of every target platform.
