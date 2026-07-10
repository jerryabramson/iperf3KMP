package edu.bu.cs683_jabramson_project.iperf3_network_tester

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform