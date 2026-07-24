package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getDest
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getSource


/**
 * Results row for the UI.
 * @param uiState the current state of the UI
 * @param monoStyle the style for the monospaced text
 */
@Preview(name = "Results Row Preview", showBackground = true, showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun ResultsRow(
    results: MutableList<String> = getSampleUiState().results,
    src: String = getSampleUiState().iperf3RunningState.localHostDetails,
    dest: String = getSampleUiState().iperf3RunningState.remoteHostDetails,
    isRunning: Boolean = false,
    isFinished: Boolean = true,
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiExecutionData = getSampleUiState(),
    monoStyle: TextStyle = mesloMonoTextStyle(),
    isWide: Boolean = false,
) {
    if (isRunning || !isFinished) return
    if (results.isEmpty()) return
    val thick = if (uiState.returnCode != 0) 5.dp else 2.dp
    val resultColor =
        if (uiState.returnCode != 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    if (results.isNotEmpty()) {
        if (!isWide) {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = thick,
                color = MaterialTheme.colorScheme.tertiary
            )
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp).fillMaxWidth()
            ) {
                Text(
                    text = src,
                    style = monoStyle.copy(fontSize = 15.sp),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Left,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = dest,
                    style = monoStyle.copy(fontSize = 15.sp),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Left,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.tertiary
                )

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(uiState.results.size) { index ->
                        Text(
                            text = results[index],
                            style = monoStyle.copy(fontSize = 16.sp),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Left,
                            color = resultColor
                        )
                    }
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)) {
                Text(
                    text = src.trim(),
                    style = monoStyle.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = dest.trim(),
                    style = monoStyle.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.primary
                )

            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
                thickness = thick,
                color = MaterialTheme.colorScheme.tertiary
            )


            if (results.size == 6) {
                val firstHalf = results.subList(0, 2)
                val secondHalf = results.subList(2, 4)
                val thirdHalf = results.subList(4, results.size)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)
                ) {
                    LazyColumn() {
                        items(firstHalf.size) { index ->
                            Text(
                                text = firstHalf[index],
                                style = monoStyle.copy(fontSize = 10.sp),
                                //modifier = Modifier.fillMaxWidth(),
                                color = resultColor
                            )
                        }
                    }
                    LazyColumn() {
                        items(secondHalf.size) { index ->
                            Text(
                                text = secondHalf[index],
                                style = monoStyle.copy(fontSize = 10.sp),
                                //modifier = Modifier.fillMaxWidth(),
                                color = resultColor
                            )
                        }
                    }
                    LazyColumn() {
                        items(thirdHalf.size) { index ->
                            Text(
                                text = thirdHalf[index],
                                style = monoStyle.copy(fontSize = 10.sp),
                                //modifier = Modifier.fillMaxWidth(),
                                color = resultColor
                            )
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(results.size) { index ->
                        Text(
                            text = results[index],
                            style = monoStyle.copy(fontSize = 8.sp),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Left,
                            color = resultColor
                        )
                    }
                }

            }
        }
    }
}



