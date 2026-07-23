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
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.model.Iperf3RunningState
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.UnitConvertedData
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.toWholeNumber
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiExecutionData


@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Running Column Section", showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun RunningColumnSection(
    progress: Float = 0.0.toFloat(),
    interval: Long = getSampleUiState().iperf3RunningState.intervalNumber,
    isRunning: Boolean = getSampleUiState().isRunning,
    latestLine: String = getSampleUiState().latestLine,
    bandWidth: String = getSampleUiState().bandWidth,
    isReverse: Boolean = getSampleInputData().isReverse,
    isOmitted: Boolean = getSampleUiState().iperf3RunningState.omitted,
    parallelStreams: Int = getSampleInputData().parallelStreams,
    durationSecs: Int = getSampleInputData().durationSecs,
    max: UnitConvertedData= getSampleUiState().iperf3RunningState.currentMax,
    min: UnitConvertedData = getSampleUiState().iperf3RunningState.currentMin,
    avg: UnitConvertedData = getSampleUiState().iperf3RunningState.currentAvg,
    preview: Boolean = true)
{
    if (!isRunning) return

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        LaunchingMessage(latestLine.isEmpty())
        val (barColor, trackColor) = progressColors(isReverse)

        if (bandWidth.isNotEmpty()) {
            //ProgressPercent(uiInputData, uiState)

            ProgressPercent(isOmitted = isOmitted, isReverse = isReverse, parallelStreams = parallelStreams, durationSecs = durationSecs, progress = progress, intervalNumber = interval)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = barColor,
                trackColor = trackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
            BandwidthDisplay(min = min, max = max, avg = avg, preview = preview, basicBandWidthString = bandWidth)
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
fun ProgressPercent(isOmitted: Boolean = getSampleUiState().iperf3RunningState.omitted,
                    isReverse: Boolean = getSampleInputData().isReverse,
                    parallelStreams: Int = getSampleInputData().parallelStreams,
                    durationSecs: Int = getSampleInputData().durationSecs,
                    intervalNumber: Long = getSampleUiState().iperf3RunningState.intervalNumber,
                    progress: Float = getSampleUiState().progress)
{
    if (!isOmitted) {
        val num = if (isReverse) (1f - progress) else progress
        val percent = (num * 100).toInt()

        //@SuppressLint("DefaultLocale")
        val iter = "iteration ${intervalNumber} of ${durationSecs}"
        Text(
            text = "${percent}% complete : $iter [$parallelStreams streams]",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )
    } else {
        Text(
            text = "Omitted Result",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color =  MaterialTheme.colorScheme.error
        )

    }
}



@Composable
fun BandwidthDisplay(omittedResult: Boolean = false,
                     stalledResult: Boolean = false,
                     basicBandWidthString: String = "",
                     max: UnitConvertedData,
                     min: UnitConvertedData,
                     avg: UnitConvertedData,
    iperf3RunningState: Iperf3RunningState = getSampleUiState().iperf3RunningState,
                     preview: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val color = if (!omittedResult && !stalledResult) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        Text(
            text = basicBandWidthString,
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
                text = toWholeNumber(min, preview).trim(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
//            Text(
//                text = toWholeNumber(med, preview),
//                color = MaterialTheme.colorScheme.primary,
//                style = MaterialTheme.typography.bodyMedium
//            )
            Text(
                text = toWholeNumber(avg, preview),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = toWholeNumber(max, preview),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}



