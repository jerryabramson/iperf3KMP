package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

//actual fun randomUUID(): String = NSUUID().UUIDString()
/*
In your androidMain (and jvmMain if you have it), use the standard Java UUID:
Kotlin
// shared/src/androidMain/kotlin/.../utils/UuidUtils.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils

import java.util.UUID

actual fun randomUUID(): String = UUID.randomUUID().toString()
3. Provide implementation for iOS
In your iosMain, use the native Apple NSUUID:

 */

