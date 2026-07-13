package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()