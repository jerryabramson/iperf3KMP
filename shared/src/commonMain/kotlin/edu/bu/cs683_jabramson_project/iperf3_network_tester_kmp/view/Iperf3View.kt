package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view




import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.ui.theme.Iperf3NetworkTesterTheme
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getDest
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getSource
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.Iperf3RunViewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiExecutionData
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData
import org.jetbrains.compose.resources.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel


/**
 * Composable function for displaying the Iperf3 screen. This screen includes
 * UI elements like a top bar, bottom bar, and a body to manage and visualize
 * Iperf3 execution and input states.
 *
 * @param viewModel The ViewModel instance of type [Iperf3RunViewModel] used
 * to manage UI state and interactions. By default, it retrieves an instance
 * using Hilt and the current ViewModelStoreOwner. Throws an error if no
 * ViewModelStoreOwner is provided.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunIperf3Screen(viewModel: Iperf3RunViewModel = viewModel { Iperf3RunViewModel() })
{
    val uiExecutionState by viewModel.uiExecutionDataStateFlow.collectAsState()
    val uiInputState by viewModel.uiInputDataStateFlow.collectAsState()
    val monoStyle = mesloMonoTextStyle()
    val fieldColors = textFieldColors()

    Iperf3NetworkTesterTheme(dynamicColor = true) {
        Scaffold(
            topBar = {
                IperfTopBar(
                    buttonAction = viewModel::launchOrCancel,
                    saveButtonAction = viewModel::saveResult,
                    currentRunning = uiExecutionState.isRunning,
                    currentFinished = uiExecutionState.isFinished,
                    currentSaved = uiExecutionState.isSaved,
                    currentIsSaving = uiExecutionState.isSaving,
                    isCompact = isCompactHeight()
                )
            },
            bottomBar = {
                IperfBottomBar(uiInputState.isDebugging, isWide = isWideWindow(), viewModel::toggleDebug)
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
            )
            {
                InputFields(
                    currentHostName = uiInputState.hostName,
                    currentPort = uiInputState.portNumber,
                    currentDuration = uiInputState.durationSecs,
                    currentParallelStreams = uiInputState.parallelStreams,
                    currentSkip = uiInputState.skip,
                    currentReverse = uiInputState.isReverse,
                    isRunning = uiExecutionState.isRunning,
                    updateHostName = viewModel::setHostName,
                    uploadDownload = viewModel::setUploadDownload,
                    launch = viewModel::launch,
                    setDuration = viewModel::setDuration,
                    setPortNumber = viewModel::setPortNumber,
                    setParallelStreams = viewModel::setParallelStreams,
                    setSkip = viewModel::setSkip,
                    colors = fieldColors,
                    style = MaterialTheme.typography.bodySmall,
                )
                //Spacer(modifier = Modifier.height(10.dp))

                /* Output rows */
                RunningColumnSection(
                    progress = uiExecutionState.progress,
                    isRunning = uiExecutionState.isRunning,
                    latestLine = uiExecutionState.latestLine,
                    bandWidth = uiExecutionState.bandWidth,
                    isReverse =  uiInputState.isReverse,
                    parallelStreams = uiInputState.parallelStreams,
                    durationSecs = uiInputState.durationSecs,
                    isOmitted = uiExecutionState.iperf3RunningState.omitted,
                    interval = uiExecutionState.iperf3RunningState.intervalNumber,
                    max = uiExecutionState.iperf3RunningState.currentMax,
                    min = uiExecutionState.iperf3RunningState.currentMin,
                    avg = uiExecutionState.iperf3RunningState.currentAvg,
                )
                ResultsRow(uiState = uiExecutionState,
                    results = uiExecutionState.results,
                    isRunning = uiExecutionState.isRunning,
                    isFinished = uiExecutionState.isFinished,
                    monoStyle = monoStyle,
                    src = getSource(uiExecutionState.iperf3RunningState),
                    dest = getDest(uiExecutionState.iperf3RunningState),
                    isWide = isWideWindow())
                IperfMessagesSection(uiState = uiExecutionState,
                    isDebugMode = uiInputState.isDebugging,
                    isWide = isWideWindow())
                ErrorSection(uiState = uiExecutionState, monoStyle)
                DebugOutputSection(
                    isDebugging = uiInputState.isDebugging,
                    isOmitted = uiExecutionState.iperf3RunningState.omitted,
                    outputLines =  uiExecutionState.outputLines,
                    monoStyle = monoStyle,
                    preview = false
                )
            }
        }
    }
}

@Composable
fun isWideWindow(): Boolean {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    val widthDp = with(density) {
        windowInfo.containerSize.width.toDp()
    }

    return widthDp >= 840.dp
}

/**
 * True when there isn't much vertical room to work with -- e.g. a phone turned
 * landscape, where a full-height top/bottom app bar can crowd out everything else.
 * Mirrors Material's own compact-height window size class breakpoint (480dp).
 */
@Composable
fun isCompactHeight(): Boolean {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    val heightDp = with(density) {
        windowInfo.containerSize.height.toDp()
    }

    return heightDp < 480.dp
}

/**
 * Custom Compose preview annotation that automatically generates previews for both light and dark themes.
 * Each preview displays the UI with the system bars visible, uses the Pixel 9 Pro device configuration,
 * and renders the background to accurately reflect theme-specific colors and contrast.
 * This annotation simplifies the development workflow by combining common preview settings into a single reusable declaration.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(name = "Light", showBackground = true, device =  Devices.PIXEL_9_PRO, showSystemUi = true)
@Preview(name = "Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL, device =  Devices.PIXEL_9_PRO, showSystemUi = true)
annotation class PreviewLightDarkWithBackground
@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDarkWithBackground
@Composable
fun ScreenTestRunning() {
    val sampleUiState: UiExecutionData = getSampleUiState(true)
    ScreenTestScaffold(uiState = sampleUiState, inputData = getSampleInputData(), isWide = false)
}

/**
 * Composable function representing the "Test Finished" screen of the application.
 *
 * The screen displays a user interface designed for when the test execution has been completed.
 * It uses various components, such as a top bar, bottom bar, and a main content body to
 * display the finished test's input and output data. The screen supports both light and dark
 * themes through a themed preview annotation.
 *
 * This function:
 * - Retrieves a sample UI state using the `getSampleUiState` method with a finished test condition.
 * - Applies the `Iperf3NetworkTesterTheme` for consistent theming across the UI components.
 * - Utilizes a `Scaffold` to organize the top bar, main content, and bottom bar.
 * - Populates the main content using the `ScreenBody` composable, passing required UI state
 *   and input data as parameters.
 *
 * Annotations:
 * - `@PreviewLightDarkWithBackground` is used to generate previews for both light and dark modes.
 * - `@OptIn(ExperimentalMaterial3Api::class)` is used to enable experimental Material Design 3 APIs.
 */
@PreviewLightDarkWithBackground()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTestFinished() {
    val sampleUiState: UiExecutionData = getSampleUiState(false)
    ScreenTestScaffold(uiState = sampleUiState, inputData = getSampleInputData(), isWide = false)
}

@Preview(name = "Wide Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL, device =  Devices.TABLET, showSystemUi = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTestWideDark() {
    val sampleUiState: UiExecutionData = getSampleUiState(false)
    ScreenTestScaffold(uiState = sampleUiState, inputData = getSampleInputData(), isWide = true)
}

@Preview(name = "Wide Light", showBackground = true, uiMode = UI_MODE_TYPE_NORMAL, device =  Devices.TABLET, showSystemUi = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTestWideLigt() {
    val sampleUiState: UiExecutionData = getSampleUiState(false)
    ScreenTestScaffold(uiState = sampleUiState, inputData = getSampleInputData(), isWide = true)
}


/**
 * Composable function that initializes and displays the main screen layout for the Iperf3 Network Tester.
 * It applies the application theme, renders a full-screen Surface, and arranges the top bar,
 * bottom bar, and main content area using a Scaffold, passing the provided states to their respective components.
 *
 * @param uiState The current execution state holding test progress, logs, bandwidth data, and status flags.
 * @param inputData The configuration data for the network test, including host, port, duration, streams, and other parameters.
 */
@Composable
fun ScreenTestScaffold(uiState: UiExecutionData,
                       inputData: UiInputData,
                       isWide: Boolean = false)
{
    Iperf3NetworkTesterTheme(dynamicColor = true) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            /* Input rows */
            Scaffold(
                topBar = { IperfTopBar(currentRunning = uiState.isRunning,  uiState.isRunning, isCompact = isWide, buttonAction = {}, saveButtonAction = {}) },
                bottomBar = { IperfBottomBar(inputData.isDebugging, isWide = isWide) {} },

                ) { padding ->
                ScreenBodyPreview(
                    padding = padding,
                    uiInputState = inputData,
                    uiExecutionState = uiState,
                    isWide = isWide,
                )

            }
        }
    }
}


/**
 * Renders the main content area of the screen, including input fields,
 * execution status, results, and debug outputs.
 *
 * @param padding Layout padding applied to the root column.
 * @param uiExecutionState Current state of the UI execution, including progress,
 *                         results, and running status. Defaults to a sample state.
 * @param uiInputState Current configuration of input parameters such as host,
 *                     port, duration, and stream settings.
 * @param updateHostName Callback invoked when the host name is updated.
 * @param uploadDownload Callback invoked for upload or download operations.
 * @param launch Callback invoked to initiate or launch the execution.
 * @param setDuration Callback invoked when the duration parameter is updated.
 * @param setPortNumber Callback invoked when the port number is updated.
 * @param setParallelStreams Callback invoked when the number of parallel streams is updated.
 * @param setSkip Callback invoked when the skip parameter is updated.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ScreenBodyPreview(padding: PaddingValues,
                      uiExecutionState: UiExecutionData = getSampleUiState(false),
                      uiInputState: UiInputData = getSampleInputData(),
                      updateHostName: (String) -> Unit = {},
                      uploadDownload: (String) -> Unit = {},
                      launch: () -> Unit = {},
                      setDuration: (String) -> Unit = {},
                      setPortNumber: (String) -> Unit = {},
                      setParallelStreams: (String) -> Unit = {},
                      setSkip: (String) -> Unit = {},
                      isWide: Boolean = false
)
{
    val monoStyle = mesloMonoTextStyle()
    val fieldColors = textFieldColors()
    Column(modifier = Modifier
        .padding(padding))
    {
        InputFields(
            currentHostName = uiInputState.hostName,
            currentPort = uiInputState.portNumber,
            currentDuration = uiInputState.durationSecs,
            currentParallelStreams = uiInputState.parallelStreams,
            currentSkip = uiInputState.skip,
            currentReverse = uiInputState.isReverse,
            isRunning = uiExecutionState.isRunning,
            updateHostName = updateHostName,
            uploadDownload = uploadDownload,
            launch = launch,
            setDuration = setDuration,
            setPortNumber = setPortNumber,
            setParallelStreams = setParallelStreams,
            setSkip = setSkip,
            colors = fieldColors,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(10.dp))

        /* Output rows */
        RunningColumnSection(
            progress = uiExecutionState.progress,
            isRunning = uiExecutionState.isRunning,
            latestLine = uiExecutionState.latestLine,
            bandWidth = uiExecutionState.bandWidth,
            isReverse =  !uiInputState.isReverse,
            parallelStreams = uiInputState.parallelStreams,
            durationSecs = uiInputState.durationSecs,
            preview = true
        )

        ResultsRow(uiState = uiExecutionState, monoStyle = monoStyle, isWide = isWide)
        IperfMessagesSection(uiState = uiExecutionState, isDebugMode = true, isWide = isWide)
        ErrorSection(uiState = uiExecutionState, monoStyle)
        DebugOutputSection(
            isDebugging = uiInputState.isDebugging,
            outputLines =  uiExecutionState.outputLines,
            isOmitted = uiExecutionState.iperf3RunningState.omitted,
        )
    }
}






