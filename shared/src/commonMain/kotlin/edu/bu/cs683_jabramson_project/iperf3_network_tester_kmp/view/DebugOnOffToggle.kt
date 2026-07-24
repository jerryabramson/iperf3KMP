package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData

//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData

@Preview(name ="DebugOnOffRadioButtonPreview", showBackground = true, device = Devices.PIXEL_9, showSystemUi = true)
@Composable
fun DebugOnOffToggle(
    currentDebugging: Boolean = false, //getSampleInputData().isDebugging,
    toggleDebug: () -> Unit = {},
    isWide: Boolean = false
) {
    val buttonColor = if (currentDebugging) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

    if (!isWide) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "iperf3 Output",
                modifier = Modifier.padding(end = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = toggleDebug, // 1. Flip state on click
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(1.dp).width(90.dp).height(60.dp)
            ) {
                if (currentDebugging) {
                    Text(
                        text = "OFF",
                        color = MaterialTheme.colorScheme.inversePrimary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Text(
                        text = "ON",
                        color = MaterialTheme.colorScheme.surface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "iperf3 Output",
                modifier = Modifier.padding(end = 10.dp),
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = toggleDebug, // 1. Flip state on click
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor
                ),
                shape = MaterialTheme.shapes.small,
                // Button's default min-height (40dp) doesn't shrink to fit a tight
                // parent -- it just gets clipped, text and all. Override both the
                // padding and the height explicitly so the text has room to render.
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                if (currentDebugging) {
                    Text(
                        text = "OFF",
                        color = MaterialTheme.colorScheme.inversePrimary,
                        style = mesloMonoTextStyle().copy(fontSize = 12.sp),
                    )
                } else {
                    Text(
                        text = "ON",
                        color = MaterialTheme.colorScheme.surface,
                        style = mesloMonoTextStyle().copy(fontSize = 12.sp),
                    )
                }
            }
        }
    }
}
