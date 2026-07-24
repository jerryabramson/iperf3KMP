package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.ui.theme.Iperf3NetworkTesterTheme
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view.RunIperf3Screen

@Composable
@Preview(name = "App",
    showBackground = true,
    device =  Devices.PIXEL_6,
    showSystemUi = true)

fun App() {
    Iperf3NetworkTesterTheme {
        RunIperf3Screen()
    }
}