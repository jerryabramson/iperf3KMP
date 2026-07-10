package edu.bu.cs683_jabramson_project.iperf3_network_tester

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return sayHello(platform.name)
    }
}