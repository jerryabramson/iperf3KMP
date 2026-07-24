package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.runner





import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.model.Iperf3RunningState

import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.Iperf3OutputMonitor
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData


import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile


/**
 * Manages the lifecycle and logic of running iPerf3-based network tests.
 * Supports advanced features like smart ramp-up, hybrid tests, and automatic bandwidth reduction.
 */
class Iperf3Executor(
    var updateProgress: (Float) -> Unit,
    var stdout: (Iperf3RunningState, Boolean) -> Unit,
    var stderr: (Iperf3RunningState, String) -> Unit,
    private val onTestComplete: () -> Unit
) {
    val tag: String = "Iperf3Executor"

    private val iperf3OutputMonitor = Iperf3OutputMonitor()

    //private var context: Context? = null


    @Volatile
    private var isIperfRunning = false

    fun getCurrentLineResult() = iperf3OutputMonitor.getCurrentLineResult()

    fun cancelTest(): Int {
        var rc = 0
        //if (context == null) return -1
        if (!isIperfRunning) return -1

        stderr(iperf3OutputMonitor.getCurrentLineResult(), "❌ Test Cancelled by User or System")
        IperfRunner.forceStop(
            createIperfCallback(
                onLine =
                    {
                        val line = it.trim().removeSuffix("\n")
                        val newLineResult = iperf3OutputMonitor.processCancelLine(line)
                        stdout(newLineResult, true)
                        //Log.d(tag, "forceStop: onLine: $line")
                    }, onError =
                    {
                        val err = it.trim().removeSuffix("\n").removePrefix("\n")
                        isIperfRunning = false
                        stderr(iperf3OutputMonitor.getCurrentLineResult(), "❌ Error: $err")
                        onTestComplete()
                        //testCompleted.complete(Unit)
                        updateProgress(1.0f)
                        rc = -1
                    },
                onComplete = {
                    isIperfRunning = false
                    onTestComplete()
                    updateProgress(1.0f)
                    //testCompleted.complete(Unit)
                }
            )
        )
        return rc


    }
    /**
     * Starts the test with the provided arguments and configuration.
     */
    suspend fun startTest(//contextParam: Context?,
                          params: UiInputData): Int
    {
        //if (contextParam == null) return -1

        //Log.d(tag, "iperf3 JNI Bridge Starting")

        /**
         * 1. Initialize the iperf3 parameters
         */
        var rc = 0

        //context = contextParam
        val reverse = if (params.isReverse)  "--reverse" else ""
        val flush = if (params.isForceFlush) "--forceflush" else ""
        val serverPort = params.portNumber
        val localTimeout = if (params.timeout != 0L) params.timeout else 3000L
        val parallelStreams = params.parallelStreams
        val durationSecs = params.durationSecs
        if (durationSecs <= 0 || durationSecs > (60 * 60 * 12)) {
            //Log.e("Iperf3Runner", "Invalid duration: $durationSecs. Must be between 1 second to 12 hours.")
            stderr(iperf3OutputMonitor.getCurrentLineResult(), "Invalid duration: ${params.durationSecs}")
            updateProgress(1.0f)
            return -1
        }
        if (serverPort !in 1..65535) {
            //Log.e("Iperf3Runner", "Invalid Server Port Number: $serverPort. Must be between 1 and 65335.")
            stderr(iperf3OutputMonitor.getCurrentLineResult(), "Invalid port number: $serverPort")
            updateProgress(1.0f)
            return -1

        }

            /**
         *  2. Set the default iperf3 temp directory to the app's cache directory'. Some Android
         *     devices may not allow writing to the external storage directory (/data/data/tmp).
         */
        val tempDirectory: String = "/data/data/tmp" //if (context != null) context!!.cacheDir.toString() else ""
        IperfRunner.setTempDir(tempDirectory)
        //Log.i(tag, "tempDirectory: $tempDirectory")

        /**
         * 3. Construct the iperf3 command line arguments.
         *    **Note** that argv[0] must be set to the program name.
         */
        val currentArgs = arrayOf(
            "iperf3",
            "--client", params.hostName,
            "--port", params.portNumber.toString(),
            reverse,
            flush,
            "--parallel", parallelStreams.toString(),
            "--connect-timeout",
            localTimeout.toString(),
            "--time", params.durationSecs.toString(),
            "--omit", params.skip.toString(),
            //"-V"
        )
        //Log.d(tag, "currentArgs: ${currentArgs.joinToString(",")}")

        /**
         * 4. Initialize the output monitor.
         */

        iperf3OutputMonitor.reset(params)
        updateProgress(calcProgress(0.0.toFloat()))

        /**
         * 5. Create a coroutine exception handler for uncaught exceptions
         */
        val handler = CoroutineExceptionHandler { _, exception ->
            stderr(iperf3OutputMonitor.getCurrentLineResult(), "🚨 Uncaught Exception: $exception")
            exception.printStackTrace()
            rc = -1
        }

        /**
         * 6. Start the actual iperf3 test using the coroutine scope.
         */
        //Log.d("Iperf3Executor: ", "startTest")
        isIperfRunning = true

        // Start Actual iPerf Test
        var lastIntervalCount = -1L
        var lastOmittedCount = -1L
        var lastNumberOfMessages = 0

        var cancelled = false
        val runJob = CoroutineScope(Dispatchers.IO + handler).launch {
            isIperfRunning = true
            //Log.d("Iperf3Executor: ", "runJob")
            IperfRunner.runIperfLive(
                currentArgs, params.durationSecs, createIperfCallback(
                    onLine =
                        {
                            val line = it.trim().removeSuffix("\n")
                            val newLineResult = iperf3OutputMonitor.processLine(line)
                            //Log.d(tag, "Callback -> onLine(\"${newLineResult.rawOutputLine}\")")
                            if (newLineResult.intervalNumber > lastIntervalCount
                                || newLineResult.totalOmitted > lastOmittedCount
                                || newLineResult.messages.size > lastNumberOfMessages) {
                                stdout(newLineResult, newLineResult.messages.size > lastNumberOfMessages)

                                // only update progress if we have a new interval and not a skipped interval
                                if (newLineResult.intervalNumber > lastIntervalCount) {
                                    updateProgress(calcProgress(lastIntervalCount.toFloat() / params.durationSecs))
                                }
                                lastIntervalCount = newLineResult.intervalNumber
                                lastOmittedCount = newLineResult.totalOmitted
                                lastNumberOfMessages = newLineResult.messages.size
                                if (!cancelled && newLineResult.consecutiveStalled >= 5) {
                                    //Log.e(tag, "iperf3 has stalled for 5 consecutive intervals!")
                                    stderr(newLineResult, "❌ iperf3 has stalled for 5 consecutive intervals!")
                                    cancelTest()
                                    cancelled = true
                                }
                            }
                        }, onError =
                        {
                            val err = it.trim().removeSuffix("\n")
                            isIperfRunning = false
                            stderr(iperf3OutputMonitor.getCurrentLineResult(), "❌ Error: $err")
                            onTestComplete()
                            updateProgress(1.0f)
                            rc = -1
                        },
                    onComplete = {
                        isIperfRunning = false
                        onTestComplete()
                        updateProgress(1.0f)
                        //testCompleted.complete(Unit)
                    }
                )
            )

        }
        isIperfRunning = false
        runJob.join()
        runJob.cancel()
        return rc //@withContext -1
    }
}




private fun createIperfCallback(
    onLine: (String) -> Unit = {},
    onError: (String) -> Unit = {},
    onComplete: () -> Unit = {}
): IperfCallback {
    return object : IperfCallback {
        override fun onOutput(line: String) = onLine(line)
        override fun onError(error: String) = onError(error)
        override fun onComplete() = onComplete()
    }
}


private fun calcProgress(p: Float): Float {
    val zeroProgress = 0.02.toFloat()
    val finishedProgress = 0.98.toFloat()
    var r = p
    if (p > finishedProgress) r = finishedProgress
    if (p < zeroProgress) r = zeroProgress
    return r
}