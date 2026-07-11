package edu.bu.cs683_jabramson_project.iperf3_network_tester.view




import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
//import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
//import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.Iperf3NetworkTesterTheme
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.mesloFontFamily
//import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.mesloFontFamily
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel




@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Iperf3Screen",
    showBackground = true,
    device =  Devices.PIXEL_9_PRO,
    showSystemUi = true)
@Composable
//fun RunIperf3Screen(viewModel: Iperf3RunViewModel)
fun RunIperf3Screen(viewModel: Iperf3RunViewModel = Iperf3RunViewModel())   //= hiltViewModel(
//    checkNotNull(
//        LocalViewModelStoreOwner.current
//    )
//{
//        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
//    }, null
//) {
{
    val uiExecutionState by viewModel.uiExecutionDataStateFlow.collectAsState()
    val uiInputState by viewModel.uiInputDataStateFlow.collectAsState()
    val monoStyle = mesloMonoTextStyle()
    val fieldColors = textFieldColors()
    val context = "foo" //LocalContext.current

    //viewModel.setContext(context)


    Scaffold(
        topBar =  { IperfTopBar(viewModel::launchOrCancel, viewModel::saveResult,  uiExecutionState) },
        bottomBar =  { IperfBottomBar(uiInputState, viewModel::toggleDebug) }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .fillMaxSize()
            .padding(horizontal = 10.dp)) {
            /* Input rows */
            InputFields(
                uiState = uiExecutionState,
                inputState = uiInputState,
                isRunning =  uiExecutionState.isRunning,
                uploadDownload = viewModel::toggleUploadDownload,
                updateHostName =  viewModel::updateHostName,
                launch =  viewModel::launchOrCancel,
                setDuration = viewModel::setDuration,
                setPortNumber = viewModel::setPortNumber,
                setParallelStreams = viewModel::setParallelStreams,
                setSkip = viewModel::setSkip,
                colors =  fieldColors,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(10.dp))

            /* Output rows */
            ResultsRow(uiState = uiExecutionState, monoStyle = monoStyle)
            RunningColumnSection(uiInputState, uiExecutionState)
            IperfMessagesSection(uiState = uiExecutionState, inputData = uiInputState, monoStyle = monoStyle)
            ErrorSection(uiExecutionState, monoStyle)
            DebugOutputSection(uiInputState, uiExecutionState, monoStyle)


        }
    }
}


@Composable
fun mesloMonoTextStyle(): TextStyle = TextStyle(
    fontFamily = mesloFontFamily(),
    fontSize = 14.sp,
    letterSpacing = 0.2.sp,
    color = MaterialTheme.colorScheme.onSurface
)


@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Iperf3Screen",
    showBackground = true,
    device =  Devices.PIXEL_9_PRO,
    showSystemUi = true)
@Composable
fun PreviewIperf3Screen() {
    val monoStyle = mesloMonoTextStyle()
    val fieldColors = textFieldColors()
    Iperf3NetworkTesterTheme(darkTheme = false, dynamicColor = true) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            /* Input rows */
            Scaffold(
                topBar =  { IperfTopBar(buttonAction =  {}, saveButtonAction = {},  uiState = getSampleUiState()) },
                bottomBar =  { IperfBottomBar(getSampleInputData()) {} },

            ) { padding ->
                Column(modifier = Modifier
                    .padding(padding))
                {
                    //HostInputRowPreview(fieldColors)
                    InputFields(uiState =  getSampleUiState(),
                        isRunning = getSampleUiState().isRunning,
                        updateHostName = {},
                        uploadDownload = {},
                        launch = {},
                        setDuration = {},
                        setPortNumber = {},
                        setParallelStreams = {},
                        setSkip = {},
                        colors = fieldColors,
                        style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))

                    /* Output rows */
                    RunningColumnSection(getSampleInputData(), getSampleUiState())
                    ResultsRow(getSampleUiState(), monoStyle)
                    IperfMessagesSection(getSampleInputData(), getSampleUiState(), monoStyle)
                    ErrorSection(getSampleUiState(), monoStyle)
                    DebugOutputSection(uiInputState = getSampleInputData(),
                        uiExecutionData = getSampleUiState(),
                        monoStyle)
                }
            }
        }
    }
}

/**
 * Preview for the run button.
 */





