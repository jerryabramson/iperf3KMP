package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.mesloFontFamily
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getHeading
//import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getHeading
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiExecutionData
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiInputData


@Preview(name = "Debug Output Section", showBackground = true,
    device = "spec:width=411dp,height=891dp", showSystemUi = true)
@Composable
fun DebugOutputSection(
    uiInputState: UiInputData = getSampleInputData(),
    uiExecutionData: UiExecutionData = getSampleUiState(),
    monoStyle: TextStyle = mesloMonoTextStyle()
) {
    if (!uiInputState.isDebugging) return
    val fontSize = 13.sp
    val style = monoStyle.copy(fontSize = fontSize)
    val color = if (uiExecutionData.resultDataInProgress.omitted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
    Column(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)) {

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth().padding(top = 5.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.tertiary
        )
        if (uiExecutionData.outputLines.isNotEmpty()) {
            Text(
                text = getHeading(),
                textAlign = TextAlign.Left,
                style = style,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp)
            )
            Box(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 2.dp)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.tertiary)
            )
            {
                //Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp)) {


                LazyColumn(modifier = Modifier.padding(bottom = 2.dp)) {
                    items(uiExecutionData.outputLines.size) { index ->
                        val str =
                            uiExecutionData.outputLines[uiExecutionData.outputLines.size - index - 1]
                        DebugOutputItem(
                            uiExecutionData.outputLines[uiExecutionData.outputLines.size - index - 1],
                            style = style,
                            color = color,
                            stalled = str.lowercase().contains("stalled"),
                            skipped = str.lowercase().contains("skipped")
                        )
                    }
                }
            }
        } else {
            Text(
                text = "No Output",
                textAlign = TextAlign.Left,
                style = style,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp)
            )

        }
    }
}

@Composable
fun DebugOutputItem(text: String,
                    style: TextStyle,
                    color: Color,
                    stalled: Boolean = false,
                    skipped: Boolean = false)
{
    Row {
        Text(
            text = text,
            textAlign = TextAlign.Left,
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
            style = style.copy(textDecoration = if (stalled) TextDecoration.LineThrough else TextDecoration.None),
            color = if (!stalled && !skipped) color else MaterialTheme.colorScheme.error

        )
    }

}