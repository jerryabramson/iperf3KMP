package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview


/**
 * Run button for the UI, part of the top bar.
 * @param isRunning whether the UI is currently running
 * @param buttonAction the action to take when the run button is pressed
 */
@Preview(name =  "Run Button")
@Composable
fun RunButton(isRunning: Boolean = true,
              buttonAction: () -> Unit = {})
{
    var buttonColor = MaterialTheme.colorScheme.primary
    if (isRunning) buttonColor = MaterialTheme.colorScheme.onErrorContainer
    Button(
        modifier = Modifier.padding(end = 10.dp),
        shape = MaterialTheme.shapes.large,
        onClick =  buttonAction,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        )
    ) {
        if (!isRunning) {
            Text(text = "Run", color = MaterialTheme.colorScheme.surface)
        } else {
            Text(text = "Stop", color = MaterialTheme.colorScheme.surface)
        }
    }
}