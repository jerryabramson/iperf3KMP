package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view

import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData

@Composable
fun IperfBottomBar(uiState: UiInputData = getSampleInputData(),
                   isCompact: Boolean = false,
                   buttonAction: () -> Unit) {
    // BottomAppBar reserves ~80dp regardless of content size, so it needs an
    // explicit override to actually shrink when vertical space is tight.
    val modifier = if (isCompact) Modifier.height(48.dp) else Modifier
    BottomAppBar(modifier = modifier) {
        ProjectBottomBar(uiState.isDebugging, isCompact, buttonAction)
    }
}