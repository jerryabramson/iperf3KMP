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

## Proposed Architecture

```
commonMain (Kotlin)
  └── expect fun formatString(format: String, vararg args: Any): String

nativeMain (Kotlin + C interop)
  ├── nativeUtils.c    — vsnprintf wrapper
  └── actual fun formatString() — calls native code via cinterop/FFI

jvmMain / androidMain (Kotlin)
  └── actual fun formatString() — uses java.util.Formatter (JVM-native, no C needed)
```

This keeps the common layer clean while using the best tool for each platform:
- **iOS/native**: C `vsnprintf` via FFI
- **Android/JVM**: Built-in `java.util.Formatter` (already optimized, well-tested)

## Impact on Research Goals

This change has no impact on the research objectives. It is purely an infrastructure improvement that:
- Reduces code complexity in the common parsing logic
- Eliminates a source of potential formatting bugs
- Aligns with the project's existing use of native C for iperf3 integration

The alternative — implementing printf-style formatting in Kotlin — would add ~200 lines of custom string manipulation code that duplicates functionality already available in the standard libraries of every target platform.
