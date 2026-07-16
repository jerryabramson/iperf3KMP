package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.runner

import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.model.ResultDataInProgress
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view.getSampleUiState
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

suspend fun SimulatedRun(
    updateProgress: (Float) -> Unit,
    stdout: (ResultDataInProgress, Boolean) -> Unit,
    stderr: (ResultDataInProgress, String) -> Unit,
    onTestComplete: () -> Unit,
    params: UiInputData
):  Int {
    var rc: Int = 0
    val r = getSampleUiState().resultDataInProgress
    r.intervalNumber = 0
    try
    {
        /**
         * Prepare to launch the iperf3 library as a suspended function.
         */
        var runningProgress = 0.0.toFloat()
        var lastIntervalCount = -1L
        var iter = 0
        var outputLineIndex = 0
        updateProgress(0f)
        val startMsg = 5
        r.messages.also { it.add(getSampleUiState().iperf3Messages[0]) }
        stdout(r, true)
        delay(2.seconds)
        while (iter++ < (startMsg + (params.durationSecs))) {
            if (iter < startMsg) {
                delay(100.milliseconds * iter)
                r.messages.also { it.add(getSampleUiState().iperf3Messages[iter]) }
                stdout(r, true)
            } else {
                delay(1.seconds)
                r.basicBandWidthString = "35.6 Mbits/sec"
                r.intervalNumber++
                r.formattedOutputLine = getSampleUiState().outputLines[outputLineIndex++]

                stdout(r, false)
                updateProgress(runningProgress)
                runningProgress = calcProgress(lastIntervalCount.toFloat() / params.durationSecs)
                lastIntervalCount = r.intervalNumber
            }
        }
        runningProgress = calcProgress (runningProgress)
        r.totalOmitted = params.skip.toLong()
        r.totalSamples = params.durationSecs.toLong()
        updateProgress(runningProgress)
        delay(400.milliseconds)
        val safeIndex1 = 5
        delay(400.milliseconds)
        r.messages.add(getSampleUiState().iperf3Messages[safeIndex1])
        stdout(r, true)

        delay(400.milliseconds)
        val safeIndex2 = 6
        r.messages.add(getSampleUiState().iperf3Messages[safeIndex2])
        stdout(r, true)

        rc = 0

    } catch (e: Exception) {
        /* Shouldn't ever get here, since guards are already in place */
        //e(tag, "Failed to run iperf3: ${e.message}", e)
        stderr(r, "Failed to run iperf3: ${e.message}")
        rc = -1
    }

    onTestComplete()
    return rc

}

private fun calcProgress(p: Float): Float {
    val zeroProgress = 0.0.toFloat()
    val finishedProgress = 1.0.toFloat()
    var r = p
    if (p > finishedProgress) r = finishedProgress
    if (p < zeroProgress) r = zeroProgress
    return r
}