package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiInputData

@Composable
fun IperfBottomBar(uiState: UiInputData = getSampleInputData(),
                   buttonAction: () -> Unit) {
    BottomAppBar {
        ProjectBottomBar(uiState, buttonAction)
    }
}