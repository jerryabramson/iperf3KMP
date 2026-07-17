package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp



interface Platform {
    val name: String
}

interface AppUUID {
    val name: String
}

interface AppLocalDateTime {
    val name: String
}

interface AppCurrentTimeMillis {
    val value: Long
}

expect fun getPlatform(): Platform

expect fun getAppUUID(): AppUUID

expect fun getAppLocalDateTime(): AppLocalDateTime

expect fun getAppCurrentTimeMillis(): AppCurrentTimeMillis