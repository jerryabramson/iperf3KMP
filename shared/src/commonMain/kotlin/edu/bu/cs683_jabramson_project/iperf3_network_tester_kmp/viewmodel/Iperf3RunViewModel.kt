package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import android.app.Application
//import android.content.Context
//import android.util.Log
//import android.util.Log.e
//import androidx.lifecycle.AndroidViewModel
//import androidx.room.Room
//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.database.ResultDataRepository
//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.database.ResultDatabase
//
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.model.Iperf3RunningState
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.model.createResultData
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.runner.simulatedRun
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getAverage
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getMaximum
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getMedian
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getMinimum
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getSampleSize
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getStandardDeviation
//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.runner.IperfTestManage
//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getAverage
//
//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getMaximum
//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getMedian
//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getMinimum
//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getSampleSize
//
//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.getStandardDeviation
//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.printLineResult
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UploadDownload.isDownload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds


object UploadDownload {
    const val UPLOAD = "Upload"
    const val DOWNLOAD = "Download"
    fun isDownload(s: String): Boolean = s.equals(DOWNLOAD, ignoreCase = true)
    fun isUpload(s: String): Boolean = s.equals(UPLOAD, ignoreCase = true)
    fun isUploadOrDownload(s: String): Boolean = isUpload(s) || isDownload(s)
}

object DefaultInputData {
    const val HOST_NAME = "jabramson.com"
    const val HOST_FIELD = "jabramson.com"
    const val PORT_NUMBER = 5201
    const val PARALLEL_STREAMS = 8
    const val DURATION = 10
    const val SKIP = 2
    const val FORCE_FLUSH = true
    const val IS_REVERSE = true
    const val TIMEOUT = 3000L
    const val IS_DEBUGGING = false
}

data class UiInputData(
    val hostName: String = DefaultInputData.HOST_NAME,
    val portNumber: Int = -1,
    val hostField: String = DefaultInputData.HOST_FIELD,
    val durationSecs: Int = -1,
    val parallelStreams: Int = -1,
    val skip: Int = -1,
    val timeout: Long = -1,
    val isForceFlush: Boolean = DefaultInputData.FORCE_FLUSH,
    val isReverse: Boolean = DefaultInputData.IS_REVERSE,
    val isDebugging: Boolean = DefaultInputData.IS_DEBUGGING
)

//lateinit var resultDatabase: ResultDatabase
//lateinit var resultDataRepository: ResultDataRepository







/**
 * Data class to hold the UI state.
 * Notice that all variables are unchangeable `val` that are also mutable.
 * I also decided to store all numeric values as strings to avoid NumberFormatException
 * issues at odd times in the UI.
 */
data class UiExecutionData(
    val outputLines: MutableList<String> = emptyList<String>().toMutableList(),
    val errorLines: MutableList<String> = emptyList<String>().toMutableList(),
    val iperf3Messages: MutableList<String> = emptyList<String>().toMutableList(),
    val results: MutableList<String> = emptyList<String>().toMutableList(),
    val latestLine: String = "",
    val bandWidth: String = "",
    val progress: Float = 0f,
    var isRunning: Boolean = false,
    val isVerbose: Boolean = false,
    var isFinished: Boolean = false,
    val isSaved: Boolean = false,
    val isSaving: Boolean = false,
    val returnCode: Int = 0,
    val lastLine: String = "",
    val resultNumber: Long = -1,
    val numberOfMessages: Int = 0,
    var iperf3RunningState: Iperf3RunningState =
        Iperf3RunningState(
            isReverse = DefaultInputData.IS_REVERSE,
            duration = DefaultInputData.DURATION,
            parallelStreams = DefaultInputData.PARALLEL_STREAMS,
            skip = DefaultInputData.SKIP,
        ),
    val context: String = "", //Context? = null,
    val isDatabaseCreated: Boolean = false,
)

/**
 * View model to Runs the native iperf3 binary extracted from
 * app assets to the app's private files' directory.
 *
  */

class Iperf3RunViewModel() : ViewModel() {

    val tag = "Iperf3RunViewModel"

    private val _uiExecutionDataStateFlow = MutableStateFlow(UiExecutionData())
    val uiExecutionDataStateFlow: StateFlow<UiExecutionData> = _uiExecutionDataStateFlow.asStateFlow()

    //
    private val _uiInputDataStateFlow = MutableStateFlow(UiInputData())
    val uiInputDataStateFlow: StateFlow<UiInputData> = _uiInputDataStateFlow.asStateFlow()



    //
//    private var iperfManager: IperfTestManage = IperfTestManage(
//        updateProgress = ::updateProgress,                       // floating point track of progress
//        stdout = ::saveOutputLine,                               // output from iperf3
//        stderr = ::saveErrorLine,                                // errors from iperf3
//        onTestComplete = { completeTest() }
//    )
//
//
//    /**
//     * Initialize the UI state.
//     */
    init {

//        Log.d(tag, "initialize UI state")
//
//        // Set up Room database and repository
//        val db = Room.databaseBuilder(
//            application,
//            ResultDatabase::class.java,
//            "result_data-db"
//        ).build()
//        resultDatabase = db
//        resultDataRepository = ResultDataRepository(db.resultDataDao())
//
        _uiInputDataStateFlow.update {
            it.copy(
                hostName = "",
                portNumber = -1,
                hostField = "",
                skip = -1,
                durationSecs = -1,
                parallelStreams = -1,
                timeout = -1,
                isForceFlush = DefaultInputData.FORCE_FLUSH,
                isReverse = DefaultInputData.IS_REVERSE,
                isDebugging = DefaultInputData.IS_DEBUGGING,
            )
        }
        _uiExecutionDataStateFlow.update {
            it.copy(
                outputLines = emptyList<String>().toMutableList(),
                errorLines = emptyList<String>().toMutableList(),
                iperf3Messages = emptyList<String>().toMutableList(),
                results = emptyList<String>().toMutableList(),
                latestLine = "",
                progress = 0f,
                isRunning = false,
                isFinished = false,
                isVerbose = false,
                isSaved = false,
                isSaving = false,
                returnCode = 0,
                lastLine = "",
                bandWidth = "",
                resultNumber = -1,
                numberOfMessages = 0,
                iperf3RunningState = Iperf3RunningState(), //.getCurrentLineResult(),
                isDatabaseCreated = false

            )
        }
    }

    //
//
//
//
//
//    /**
//     * Callback to save an output line from the iperf3 binary.
//     * @param iperf3RunningState The output line from the process execution.
//     */
    fun saveOutputLine(iperf3RunningState: Iperf3RunningState, newMessage: Boolean = false) {
//        val lineResultStr = printLineResult(iperf3RunningState)
//        Log.d(tag, "viewModel: saveOutputLine() -> $lineResultStr")
//        val lastMessages = iperf3RunningState.messages.toMutableList()
//
        if (newMessage) {
//            lastMessages.forEach { Log.d(tag, "lastMessages: $it") }
            _uiExecutionDataStateFlow.update {
                it.copy(
                    iperf3Messages = iperf3RunningState.messages.toMutableList(),
                    numberOfMessages = 5, //lastMessages.size,
                    iperf3RunningState = iperf3RunningState
                )
            }
        } else {
            _uiExecutionDataStateFlow.update { it ->
                it.copy(
                    lastLine = it.latestLine,
                    bandWidth = iperf3RunningState.basicBandWidthString,
                    latestLine = iperf3RunningState.formattedOutputLine,
                    outputLines = it.outputLines.also {
                        if (iperf3RunningState.formattedOutputLine.isNotEmpty()) {
                            it.add(iperf3RunningState.formattedOutputLine)
                        }
                    },
                    resultNumber = iperf3RunningState.intervalNumber,
                    iperf3Messages = it.iperf3Messages.toMutableList(),
                    iperf3RunningState = iperf3RunningState
                )
            }
//
        }
    }

    //
//    /**
//     * Callback to save an error line from the iperf3 binary.
//     * @param aLine The error line from the process execution.
//     */
    fun saveErrorLine(iperf3RunningState: Iperf3RunningState, aLine: String = "") {
        // Log.d(tag, "stderr: $aLine")
        _uiExecutionDataStateFlow.update { data ->
            data.copy(
                errorLines = data.errorLines.also { it.add(aLine) },
                iperf3RunningState = iperf3RunningState,
                latestLine = iperf3RunningState.rawOutputLine
            )
        }
    }

    //
    fun launchOrCancel() {
        if (!_uiExecutionDataStateFlow.value.isRunning) launch() else cancel()
    }

    //
    fun saveResult() {
        if (!_uiExecutionDataStateFlow.value.isSaved) {
            viewModelScope.launch(Dispatchers.IO) {
                if (_uiExecutionDataStateFlow.value.iperf3RunningState.totalSamples > 0) {
                    val current =
                        createResultData(_uiExecutionDataStateFlow.value.iperf3RunningState)
                    if (current.reportedIterations > 0) {
                        _uiExecutionDataStateFlow.update {
                            it.copy(isSaving = true)
                        }

                        delay(3.seconds)
//                      resultDatabase.resultDataDao().insert(current)
//                      Log.d(tag, "Saved result: ${current.guid}")
                        _uiExecutionDataStateFlow.update {
                            it.copy(isSaved = true, isSaving = false)
                        }
                    }
                }
            }
        } else {
          TODO("Export as CSV")
        }
    }

    /**
     * Launch the iperf3 binary (notice that this is an asynchronous operation).
     */
    fun launch() {
        val tempHostName = _uiInputDataStateFlow.value.hostName.ifEmpty { DefaultInputData.HOST_NAME }
        val tempPortNumber =
            if (_uiInputDataStateFlow.value.portNumber == -1)
                DefaultInputData.PORT_NUMBER
            else _uiInputDataStateFlow.value.portNumber
        val tempHostField = "${tempHostName}:${tempPortNumber}"
        _uiInputDataStateFlow.update {
            it.copy(
                // Ensure the user can see selections during the run
                hostName = tempHostName,
                portNumber = tempPortNumber,
                hostField = tempHostName,
                durationSecs = if (it.durationSecs == -1) DefaultInputData.DURATION else it.durationSecs,
                parallelStreams = if (it.parallelStreams == -1) DefaultInputData.PARALLEL_STREAMS else it.parallelStreams,
                skip = if (it.skip == -1) DefaultInputData.SKIP else it.skip,
                timeout = if (it.timeout == -1L) DefaultInputData.TIMEOUT else it.timeout,
                isForceFlush = it.isForceFlush,
                isReverse = it.isReverse,
                isDebugging = it.isDebugging
            )
        }

        // Update the UI active running state with empty values for the start of the test.
        _uiExecutionDataStateFlow.update { it ->
            it.copy(
                isRunning = true,
                isFinished = false,
                outputLines = it.outputLines.also { it.clear() },
                errorLines = it.errorLines.also { it.clear() },
                iperf3Messages = it.iperf3Messages.also { it.clear() },
                results = emptyList<String>().toMutableList(),
                bandWidth = "",
                latestLine = "",
                progress = 0f,
                lastLine = "",
                resultNumber = -1,
                returnCode = 0,
                isSaved = false,
                isSaving = false
            )
        }
        /*
       * Launch the iperf3 binary asynchronously.
       * I decided to wrap the async launch in a separate function
       * to make the code more explicit.
       */
//     Log.d(tag, "Async Launch Started")


        viewModelScope.launch { runIperf3() }
//      Log.d(tag, "Async Launch Completed")
    }

//
    /**
     * Return a valid number or reset to base  state if it is
     * either empty, or an invalid number.
     * @param s The string to convert to an integer.
     * @return The integer value of the string, or -1 if invalid or empty.
     */
    fun myInt(s: String, o: Int): Int = if (s.isEmpty() || s.equals("0")) { -1 } else { try { s.toInt() } catch (e: Exception) { o } }

    /**
     * Cancel the running test. It is important to give the running iperf3
     * threads the mechanism to exit cleanly, but telling the remote iperf3
     * server that the connection should be closed.
     * Otherwise, the server stays in the busy state for an indeterminate duration.
     */
    fun cancel() {
        //Log.d(tag, "Async cancel Started")
        _uiExecutionDataStateFlow.update {
            it.copy(
                returnCode = -1,
                isRunning = false,
                isFinished = true
            )
        }
        completeTest()
//      Log.d(tag, "Async Cancel Started")
//      viewModelScope.launch {cancelTest() }
//      Log.d(tag, "Async Cancel Completed")
    }

    //
    fun cancelTest(): Int {
        var rc: Int
        try {
            rc = -1 //iperfManager.cancelTest()
        } catch (e: Exception) {
            /* Shouldn't ever get here, since guards are already in place */
            //e(tag, "Failed to cancel iperf3: ${e.message}", e)
            saveErrorLine(_uiExecutionDataStateFlow.value.iperf3RunningState, "Failed to cancel iperf3: ${e.message}")
            rc = -1
        }
        return rc
    }

    //
//    /**
//     * Run the iperf3 binary.
//     * This must be a suspend function called from a coroutine.
//     * @return The return code from the iperf3 binary.
//     */
    suspend fun runIperf3(): Int {

        val rc = simulatedRun(
            updateProgress = ::updateProgress,                       // floating point track of progress
            stdout = ::saveOutputLine,                               // output from iperf3
            stderr = ::saveErrorLine,
            params = _uiInputDataStateFlow.value,
            onTestComplete = ::completeTest)

        //Update the UI state to show that the test is finished and
        // Provide the return code to the UI.
        if (rc != 0) {
            // Only need this on failure conditions
            _uiExecutionDataStateFlow.update { data -> data.copy(results = data.results.also { it.add("Error: Return Code = $rc") }) }
        }
        val outputCount = _uiExecutionDataStateFlow.value.iperf3RunningState.intervalNumber
        if (outputCount > 0) {
            val exe = getSampleSize(_uiExecutionDataStateFlow.value.iperf3RunningState)
            val max = getMaximum(_uiExecutionDataStateFlow.value.iperf3RunningState)
            val min = getMinimum(_uiExecutionDataStateFlow.value.iperf3RunningState)
            val avg = getAverage(_uiExecutionDataStateFlow.value.iperf3RunningState)
            val med = getMedian(_uiExecutionDataStateFlow.value.iperf3RunningState)
            val std = getStandardDeviation(_uiExecutionDataStateFlow.value.iperf3RunningState)
            //val src = getSource(_uiExecutionDataStateFlow.value.resultData)
            //val dest = getDest(_uiExecutionDataStateFlow.value.resultData)
            if (!exe.isEmpty()) _uiExecutionDataStateFlow.update { it -> it.copy(results = it.results.also { it.add(exe) }) }
            if (!max.isEmpty()) _uiExecutionDataStateFlow.update { it -> it.copy(results = it.results.also { it.add(max) }) }
            if (!min.isEmpty()) _uiExecutionDataStateFlow.update { it -> it.copy(results = it.results.also { it.add(min) }) }
            if (!avg.isEmpty()) _uiExecutionDataStateFlow.update { it -> it.copy(results = it.results.also { it.add(avg) }) }
            if (!med.isEmpty()) _uiExecutionDataStateFlow.update { it -> it.copy(results = it.results.also { it.add(med) }) }
            if (!std.isEmpty()) _uiExecutionDataStateFlow.update { it -> it.copy(results = it.results.also { it.add(std) }) }
        } else {
            _uiExecutionDataStateFlow.update { it -> it.copy(results = it.results.also { it.add("No Results") }) }
        }


        // Provide the return code to the UI.
        // Clear the hostName field for the UI.
        _uiExecutionDataStateFlow.update {
            it.copy(
                returnCode = rc,
                isRunning = false,
                isFinished = true,
            )
        }
        completeTest()
        return rc
    }

    fun completeTest() {
        _uiInputDataStateFlow.update {
            it.copy(
                hostName = if (it.hostName != DefaultInputData.HOST_NAME) it.hostName else "",
                hostField = if (it.hostField != DefaultInputData.HOST_FIELD) it.hostField else "",
                skip = if (it.skip != DefaultInputData.SKIP) it.skip else -1,
                durationSecs = if (it.durationSecs != DefaultInputData.DURATION) it.durationSecs else -1,
                parallelStreams = if (it.parallelStreams != DefaultInputData.PARALLEL_STREAMS) it.parallelStreams else -1,
                portNumber = if (it.portNumber != DefaultInputData.PORT_NUMBER) it.portNumber else -1,
                isForceFlush = it.isForceFlush
            )
        }
    }

    //
    /**
     * Callback to update the progress bar.
     * @param newProgress The new progress value.
     * We implement the progress bar as a floating point value between 0.0 and 1.0.
     * If uploading, the progress bar goes from 0.0 to 1.0 [left to right]
     * If downloading, the progress bar goes from 1.0 to 0.0 [right to left]
     */
    fun updateProgress(newProgress: Float) {
        val normalizedProgress = if (!_uiInputDataStateFlow.value.isReverse) newProgress else 1.0f - newProgress
        _uiExecutionDataStateFlow.update {
            it.copy(progress = normalizedProgress)
        }
    }

    /**
     * User entered a new host name.
     * @param hostField The new host name.
     */
    fun setHostName(hostField: String) {
        if (!hostField.isEmpty()) {
            _uiInputDataStateFlow.update {
                it.copy(
                    hostField = hostField,
                    hostName = hostField
                )
            }
        } else {
            // Reset to base state if user clears field
            _uiInputDataStateFlow.update {
                it.copy(hostField = "", hostName = "")
            }
        }
    }

    /**
     * User entered a new duration.
     * Notice that we do not allow changes to the user interface
     * if the resulting number is invalid.
     * @param portNumber new port number
     */
    fun setPortNumber(portNumber: String) {
        _uiInputDataStateFlow.update {
            it.copy(portNumber = myInt(portNumber, _uiInputDataStateFlow.value.portNumber))
        }
    }

    /**
     * User entered a new duration.
     * Notice that we do not allow changes to the user interface
     * if the resulting number is invalid.
     * @param duration The new value for duration.
     */
    fun setDuration(duration: String) {
        _uiInputDataStateFlow.update {
            it.copy(durationSecs = myInt(duration, _uiInputDataStateFlow.value.durationSecs))
        }
    }

    /**
     * The user entered a new value for the omitted field.
     * Notice that we do not allow changes to the user interface
     * if the resulting number is invalid.
     * @param skip The new value for omitted.
     */
    fun setSkip(skip: String) {
        _uiInputDataStateFlow.update {
            it.copy(skip = myInt(skip, _uiInputDataStateFlow.value.skip))
        }
    }

    /**
     * User entered a new value for parallel streams.
     * Notice that we do not allow changes to the user interface
     * if the resulting number is invalid.
     * @param str The new value for parallel streams.
     */
    fun setParallelStreams(str: String) {
        _uiInputDataStateFlow.update {
            it.copy(parallelStreams = myInt(str, _uiInputDataStateFlow.value.parallelStreams))
        }
    }

    /**
     * User chose radio button for upload/download.
     * @param str The new value for upload/download.
     */
    fun setUploadDownload(str: String) {
        _uiInputDataStateFlow.update {
            it.copy(isReverse = isDownload(str))
        }
    }


    /**
     * User toggled force flush.
     * @param forceFlush The new value for forceflush.
     */
    fun setForceFlush(forceFlush: Boolean) {
        _uiInputDataStateFlow.update {
            it.copy(isForceFlush = forceFlush)
        }
    }

    /**
     * Called at the start of the composable view
     */
    fun setContext(context: String) {  //Context) {
        _uiExecutionDataStateFlow.update {
            it.copy(context = context)
        }
    }

    /**
     * User pressed the 'show iperf3 output' button to toggle debug
     */
    fun toggleDebug() {
        val newState = !_uiInputDataStateFlow.value.isDebugging
        _uiInputDataStateFlow.update {
            it.copy(isDebugging = newState)
        }
    }
}








