package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view

//import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.toWholeNumber
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiExecutionData


@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Running Column Section", showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun RunningColumnSection(
    uiInputData: UiInputData = getSampleInputData(),
    uiState: UiExecutionData = getSampleUiState(),
) {
    if (!uiState.isRunning) return

    Column(modifier = Modifier.fillMaxWidth()) {
        LaunchingMessage(uiState.latestLine.isEmpty())
        val (barColor, trackColor) = progressColors(uiInputData.isReverse)

        if (uiState.bandWidth.isNotEmpty()) {
            ProgressPercent(uiInputData, uiState)
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = barColor,
                trackColor = trackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
            BandwidthDisplay(uiState)
        }
    }
}


@Composable
private fun LaunchingMessage(show: Boolean) {
    if (show) {
        Text("Launching iperf3 ...",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
        )
        HorizontalDivider(
            thickness = 10.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}


@Composable
fun progressColors(isReverse: Boolean): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    return if (!isReverse) {
        MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.surfaceDim
    } else {
        MaterialTheme.colorScheme.surfaceDim to MaterialTheme.colorScheme.primary
    }
}



@Composable
fun ProgressPercent(uiInputData: UiInputData = getSampleInputData(),
                    uiExecutionState: UiExecutionData = getSampleUiState()) {
    val num = if (uiInputData.isReverse) 1f - uiExecutionState.progress else uiExecutionState.progress
    val percent = (num * 100).toInt()

    //@SuppressLint("DefaultLocale")
    val iter = "iteration ${uiExecutionState.resultDataInProgress.intervalNumber} of ${uiInputData.durationSecs}"


    val streams = uiInputData.parallelStreams
    Text(
        text = "${percent}% complete : $iter [$streams streams]",
        modifier = Modifier.fillMaxWidth(),
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
    )
}



@Composable
fun BandwidthDisplay(uiState: UiExecutionData = getSampleUiState()) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val omittedResult = uiState.resultDataInProgress.omitted
        val stalledResult = uiState.resultDataInProgress.stalled
        val color = if (!omittedResult && !stalledResult) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        Text(
            text = uiState.resultDataInProgress.basicBandWidthString,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, end = 2.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
        val max = uiState.resultDataInProgress.currentMax
        val min = uiState.resultDataInProgress.currentMin
        val avg = uiState.resultDataInProgress.currentAvg

        //val med = uiState.lineResult.currentMedian
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, end = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Min",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelMedium,
            )
//            Text(
//                text = "Mean",
//                color = MaterialTheme.colorScheme.secondary,
//                style = MaterialTheme.typography.labelMedium
//            )
            Text(
                text = "Avg",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "Max",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelMedium
            )

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, end = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = toWholeNumber(min).trim(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
//            Text(
//                text = toWholeNumber(med),
//                color = MaterialTheme.colorScheme.primary,
//                style = MaterialTheme.typography.bodyMedium
//            )
            Text(
                text = toWholeNumber(avg),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = toWholeNumber(max),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}



