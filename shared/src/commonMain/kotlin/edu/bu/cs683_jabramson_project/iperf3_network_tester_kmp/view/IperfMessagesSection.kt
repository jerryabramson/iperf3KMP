package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiExecutionData


@Preview(name = "Iperf Messages Section", showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun IperfMessagesSection(
    isDebugMode: Boolean = true,
    uiState: UiExecutionData = getSampleUiState(),
    monoStyle: TextStyle = mesloMonoTextStyle()
) {
    val isActive = (uiState.isRunning && !uiState.isFinished && uiState.latestLine.isEmpty())
    val isErrors = uiState.errorLines.isNotEmpty()
    val fontSize = 12.sp
    val style = monoStyle.copy(fontSize = fontSize)
    Column(modifier = Modifier.fillMaxWidth()) {
        if (isDebugMode || isActive || isErrors)
        {
            val defaultColor = if (uiState.returnCode != 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            val defaultThickness = if (uiState.returnCode != 0) 4.dp else 1.dp
            HorizontalDivider(
                thickness = defaultThickness,
                color = defaultColor
            )
            if (uiState.iperf3Messages.isNotEmpty()) {
                // Capped so this can't silently claim all remaining vertical space
                // in the non-scrolling parent Column the way an unbounded
                // LazyColumn otherwise would -- it scrolls within the cap instead.
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 48.dp)) {
                    items(uiState.iperf3Messages.size) { index ->
                        IperfMessageItem(uiState.iperf3Messages[index], style)
                    }
                }
            }

            if (isActive && !isErrors) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally).size(16.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
private fun IperfMessageItem(text: String, style: TextStyle) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            textAlign = TextAlign.Left,
            style = style,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


