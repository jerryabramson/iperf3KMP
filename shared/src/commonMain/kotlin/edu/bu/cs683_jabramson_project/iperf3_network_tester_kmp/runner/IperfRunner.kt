package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.runner

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object IperfRunner {

    var cancel: Boolean = false
    //@JvmStatic
    fun setTempDir(tempDir: String) {
        println("setTempDir: $tempDir")
    }


    //@JvmStatic
    fun forceStop(callback: IperfCallback) {
        println("forceStop")
        cancel = true
    }

    //@JvmStatic
    suspend fun runIperfLive(arguments: Array<String>, count: Int, callback: IperfCallback) {
        cancel = false
        val outputExample = getSampleOutputData()
        var c = count - 1
        for (line in outputExample) {
            callback.onOutput(line)
            delay(100.milliseconds)
            if (line.contains("SUM") && !line.contains("omitted") && (c-- <= 0)) break
            if (cancel) return
        }
        callback.onOutput("iperf Done.")
        callback.onOutput("{iPerf JNI} 🚀 Test completed successfully.")
    }
}