package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices.PIXEL_6
import androidx.compose.ui.tooling.preview.Preview


/**
 * Run button for the UI, part of the top bar.
 * @param isRunning whether the UI is currently running
 * @param buttonAction the action to take when the run button is pressed
 */
@Preview(name =  "Run Button", showBackground = true, device = PIXEL_6, showSystemUi = true)
@Composable
fun RunButton(isRunning: Boolean = true,
              buttonAction: () -> Unit = {},
              isCompact: Boolean = false)
{
    var buttonColor = MaterialTheme.colorScheme.primary
    if (isRunning) buttonColor = MaterialTheme.colorScheme.onErrorContainer
    Button(
        // Button's default min-height (40dp) doesn't shrink to fit a tight top
        // bar -- it just gets clipped, text and all. Override explicitly.
        modifier = if (isCompact) Modifier.height(28.dp) else Modifier.padding(end = 10.dp),
        shape = MaterialTheme.shapes.large,
        onClick =  buttonAction,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        ),
        contentPadding = if (isCompact) PaddingValues(horizontal = 8.dp, vertical = 2.dp) else ButtonDefaults.ContentPadding
    ) {
        val style = if (isCompact) mesloMonoTextStyle().copy(fontSize = 9.sp) else MaterialTheme.typography.labelLarge
        if (!isRunning) {
            Text(text = "Run", color = MaterialTheme.colorScheme.surface, style = style)
        } else {
            Text(text = "Stop", color = MaterialTheme.colorScheme.surface, style = style)
        }
    }
}