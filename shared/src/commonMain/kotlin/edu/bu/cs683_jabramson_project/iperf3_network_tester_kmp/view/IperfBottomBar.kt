package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view

import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData

@Composable
fun IperfBottomBar(uiState: UiInputData = getSampleInputData(),
                   buttonAction: () -> Unit) {
    BottomAppBar {
        ProjectBottomBar(uiState.isDebugging, buttonAction)
    }
}