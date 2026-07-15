package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData

//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData

@Preview(name ="DebugOnOffRadioButtonPreview", showBackground = true, device = Devices.PIXEL_9, showSystemUi = true)
@Composable
fun DebugOnOffToggle(
    currentDebugging: Boolean = getSampleInputData().isDebugging,
    toggleDebug: () -> Unit = {}
) {
    val buttonColor = if (!currentDebugging) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    Column(horizontalAlignment = Alignment.End) {
        Text(text = "iperf3 Output",
            modifier = Modifier.padding(end = 10.dp),
            style = MaterialTheme.typography.bodySmall)
        Button(
            onClick =  toggleDebug, // 1. Flip state on click
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.padding(1.dp)//.width(240.dp).height(50.dp)
        ) {
            if (currentDebugging) {
                Text(text = "Turn Off",
                    color = MaterialTheme.colorScheme.surface,
                    style = MaterialTheme.typography.labelSmall)
            } else {
                Text(text = "Turn On",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
