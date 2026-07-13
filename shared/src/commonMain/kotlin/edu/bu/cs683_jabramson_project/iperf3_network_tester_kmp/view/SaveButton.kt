package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view


import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Preview("Save Button Preview", showBackground = true)
@Composable
fun SaveButton(isRunning: Boolean = false,
               isFinished: Boolean = true,
               isSaved: Boolean = false,
               saveButtonAction: () -> Unit = {})
{
    var buttonColor = MaterialTheme.colorScheme.primary
    var buttonText = " Save\nResults"
    var buttonTextColor = MaterialTheme.colorScheme.surface
    val fontSize = 9.sp
    val style = mesloMonoTextStyle().copy(fontSize = fontSize)

    if (isRunning) {
        buttonText = "Running"
    }
    if (isSaved && isFinished && !isRunning) {
        buttonColor = MaterialTheme.colorScheme.onSurfaceVariant
        buttonText = "Export"
        buttonTextColor = MaterialTheme.colorScheme.onSurface
    }
    Button(
        //modifier = Modifier.padding(end = 4.dp),
        //shape = if (!isRunning) MaterialTheme.shapes.medium else MaterialTheme.shapes.small,
        //shape = MaterialTheme.shapes.extraSmall,
        onClick = saveButtonAction,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        ),
        enabled = !isRunning && isFinished
    ) {
        Text(text = buttonText, color = buttonTextColor, style = style)
    }

}