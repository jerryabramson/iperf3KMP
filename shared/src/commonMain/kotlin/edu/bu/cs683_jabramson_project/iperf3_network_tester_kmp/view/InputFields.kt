package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view


//import android.R.attr.label
//import android.R.attr.singleLine
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiExecutionData
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.DefaultInputData


/**
 * Host input row for the UI.
 * Consists of:
 *   1. Text field for the host name,
 *   2. Numeric field for the duration
 *   3. Settings section - upload/download

 * @param uiState the current state of the UI
 * @param colors the colors for the text fields
 * @param style the style for the monospaced text
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Host Input Row", showBackground = true, device = "spec:width=411dp,height=891dp", showSystemUi = true)
@Composable
fun InputFields(
    uiState: UiExecutionData = getSampleUiState(),
    inputState: UiInputData = getSampleInputData(),
    isRunning: Boolean = false,
    updateHostName: (String) -> Unit = {},
    uploadDownload: (String) -> Unit = {},
    launch: () -> Unit = {},
    setDuration: (String) -> Unit = {},
    setPortNumber: (String) -> Unit = {},
    setParallelStreams: (String) -> Unit = {},
    setSkip: (String) -> Unit = {},
    colors: androidx.compose.material3.TextFieldColors = textFieldColors(),
    style: TextStyle = MaterialTheme.typography.bodySmall)
{
/*
    val placeHolder = if (currentValue == -1) defaultValue.toString() else currentValue.toString()
    val valString = if (currentValue == -1) "" else currentValue.toString()
 */

    val valString: String
    val placeHolder: String
    if (inputState.hostName.trim().isEmpty() || inputState.hostName.trim() == DefaultInputData.HOST_NAME.trim()) {
        valString = ""
        placeHolder = DefaultInputData.HOST_NAME.trim()
    } else {
        valString = inputState.hostName.trim()
        placeHolder = inputState.hostName.trim()
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(start =6.dp, end =6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
        ) {
            TextField(
                value = valString,
                onValueChange = updateHostName,
                enabled = !isRunning,
                placeholder = {
                    Text(
                        text =  placeHolder,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                },
                modifier = Modifier.width(width = 180.dp)
                    .padding(end = 8.dp),
                label = {
                    Text(
                        text =  "iPerf3 Server",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                colors = colors,
                singleLine = true,
                //keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { launch() }),
                //keyboardOptions = KeyboardOptions(
                //    keyboardType = KeyboardType.Uri,
                //    imeAction = ImeAction.Done
                //)
            )

            GenericNumericField(
                currentValue = inputState.portNumber,
                onValueChange = setPortNumber,
                enabled = !uiState.isRunning,
                defaultValue = DefaultInputData.PORT_NUMBER,
                label = "Port",
                modifier = Modifier.width(width = 90.dp)
                    .padding(end = 8.dp),
                colors = colors
            )

            GenericNumericField(
                currentValue = inputState.durationSecs,
                onValueChange = setDuration,
                enabled = !uiState.isRunning,
                defaultValue = DefaultInputData.DURATION,
                label = "Duration",
                modifier = Modifier.width(width = 120.dp),
                         //.padding(end = .dp),
                colors = colors
            )


        }
        Row(
            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),//.fillMaxWidth().
        ) {

            GenericNumericField(
                currentValue = inputState.parallelStreams,
                onValueChange = setParallelStreams,
                enabled = !isRunning,
                defaultValue = DefaultInputData.PARALLEL_STREAMS,
                label = "Streams",
                modifier = Modifier.padding(end = 8.dp).width(width = 120.dp),
                //colors = colors
            )
            GenericNumericField(
                currentValue = inputState.skip,
                onValueChange = setSkip,
                enabled = !isRunning,
                defaultValue = DefaultInputData.SKIP,
                label = "Omitted Results",
                modifier = Modifier.width(width = 130.dp),//.padding(end = 8.dp).
                colors = colors,
                )
            UploadDownload(inputData= inputState, uiState = uiState, uploadDownload)
        }
    }
}

