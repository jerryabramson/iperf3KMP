package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
// expect fun randomUUID()
// expect fun format(fmt: String, varargs ...)
// String.format("%12.12s", "hello")
// "%12.12s".format("hello")

// Planning
// ---------
// Next Week
// 1. UUID and format() - way to do - straightforward
// 2. RoomDB - TBD - Normalization efforts - not hard
// Stretch Goal
// 3. Integrate native iperf3.21 into KMP - BIGGEST EFFORT
