package edu.bu.cs683_jabramson_project.iperf3_network_tester

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "iperf3KMP",
    ) {
        App()
    }
}