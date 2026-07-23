package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils

//import android.annotation.SuppressLint
//import androidx.compose.ui.text.intl.Locale

import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.getAppUUID
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.model.Iperf3RunningState
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData
//import java.util.Locale
import kotlin.math.roundToLong
import kotlin.math.sqrt



/**
 * Parses iperf3 stdout lines and accumulates bandwidth statistics.
 *
 * Replaces the old static-singleton Java class: MonitorIPerf3Output with an instance-based
 * class, so the state is explicit, testable, and not shared across tests.
 */
class Iperf3OutputMonitor {


    // -- accumulated state (private, no static fields) --
    private var currentIperf3RunningState: Iperf3RunningState = Iperf3RunningState()
    private var gathered = false
    private var lastResult = ""
    private var summaryResults = false
    private var finished = false
    private var lastOmitted = false
    private var isSingleThread = true
    private var historicalResults: MutableList<Double> = emptyList<Double>().toMutableList()

    /** Reset all accumulated state for a new test run. */
   fun reset(params: UiInputData) {

        currentIperf3RunningState = Iperf3RunningState(
            isReverse = params.isReverse,
            duration = params.durationSecs,
            parallelStreams = params.parallelStreams,
            skip = params.skip,
            guid = getAppUUID()

        )
        gathered = false
        lastResult = ""
        summaryResults = false
        finished = false
        lastOmitted = false
        isSingleThread = params.parallelStreams == 1
        historicalResults = emptyList<Double>().toMutableList()
    }


    fun processCancelLine(line: String): Iperf3RunningState {
        currentIperf3RunningState.messages.add(line)
        return currentIperf3RunningState
    }
    /** Parse one iperf3 output line and return all extracted data at once. */
    //@SuppressLint("DefaultLocale"
    fun processLine(line: String): Iperf3RunningState {
        val cleanLine = line.replace("\n", "")
        currentIperf3RunningState.rawOutputLine = cleanLine
        val firstLeftBracket = cleanLine.indexOf('[')
        val firstRightBracket = cleanLine.indexOf(']')
        if (firstLeftBracket in 0..<firstRightBracket) {
            val id = cleanLine.substring(firstLeftBracket + 1, firstRightBracket)
            val restOfLine = cleanLine.substring(firstRightBracket + 1).split(Regex("[ \\t]+"))
            // Connection-info line (e.g. "[ ID ] Interval...")
            if (id == "ID") {
                if (!gathered) {
                    gathered = true
                    currentIperf3RunningState.intervalNumber = 0
                }
            } else {
                // Pre-gathering: extract connection details or collect messages
                if (!gathered) {
                    if (restOfLine.size == 10) {
                        currentIperf3RunningState.localHost = restOfLine[2].trim()
                        currentIperf3RunningState.localPort = restOfLine[4].trim().toLongOrNull() ?: -1L
                        currentIperf3RunningState.remoteHost = restOfLine[7].trim()
                        currentIperf3RunningState.remotePort = restOfLine[9].trim().toLongOrNull() ?: -1L
                        currentIperf3RunningState.connectedString = restOfLine[5].trim()
                        currentIperf3RunningState.timeout = restOfLine[6].trim()
                        val localHostPort =  formatString("%s:%-5d",currentIperf3RunningState.localHost, currentIperf3RunningState.localPort)
                        val remoteHostPort = formatString("%s:%-5d", currentIperf3RunningState.remoteHost, currentIperf3RunningState.remotePort)
                        currentIperf3RunningState.localHostDetails = "Local Host:port $localHostPort"
                        currentIperf3RunningState.remoteHostDetails = "Remote Host:port $remoteHostPort"
                        currentIperf3RunningState.rawLocalHostDetails = localHostPort
                        currentIperf3RunningState.rawRemoteHostDetails = remoteHostPort
                        currentIperf3RunningState.messages.add(" Local Host:port $localHostPort")
                        currentIperf3RunningState.messages.add("Remote Host:port $remoteHostPort")
                        gathered = true
                    }
                } else {
                    // Post-gathering: interval / summary data
                    if (restOfLine.size >= 7) {
                        val intervalString = restOfLine[1].trim()
                        val bitRateString = restOfLine[5].trim()
                        val bitRateUnitString = restOfLine[6].trim()
                        var sendOrReceive = ""
                        for (i in 7..10) {
                            if (i < restOfLine.size) {
                                sendOrReceive = restOfLine[i].trim()
                            }
                        }
                        if (sendOrReceive.isNotEmpty()) {
                            val lower = sendOrReceive.lowercase()
                            if (!lower.contains("sender") && !lower.contains("receive") && !lower.contains("omit")) {
                                sendOrReceive = ""
                            }
                        }

                        // Only process SUM lines or single-thread intervals
                        if (id.contains("SUM") || isSingleThread) {
                            val bitRateValue = bitRateString.toDoubleOrNull() ?: -1.0
                            currentIperf3RunningState.currentBandWidth = UnitConvertedData(bitRateValue, bitRateUnitString)
                            currentIperf3RunningState.basicBandWidthString = "$bitRateString $bitRateUnitString"
                            val timeLabel: String
                            when (sendOrReceive.lowercase()) {
                                "(omitted)" -> {
                                    lastOmitted = true
                                    timeLabel = "skipped"
                                    currentIperf3RunningState.totalOmitted++
                                    currentIperf3RunningState.omitted = true
                                    currentIperf3RunningState.basicBandWidthString = "${currentIperf3RunningState.basicBandWidthString} (omitted)"
                                }

                                "sender", "receiver" -> {
                                    lastOmitted = false
                                    currentIperf3RunningState.omitted = false
                                    if (!finished) {
                                        finished = true
                                        summaryResults = true
                                        currentIperf3RunningState.totalSamples++
                                        currentIperf3RunningState.currentBandWidth = UnitConvertedData(bitRateValue, bitRateString)
                                        currentIperf3RunningState.currentAvg = UnitConvertedData(bitRateValue, bitRateUnitString)
                                    }
                                    timeLabel = sendOrReceive
                                }
                                else -> {
                                    if (lastOmitted) {
                                        currentIperf3RunningState.intervalNumber = 0
                                        currentIperf3RunningState.totalSamples = 0
                                    }
                                    lastOmitted = false
                                    val intervalParts = intervalString.split("-")
                                    val expectedIntervalLong = currentIperf3RunningState.intervalNumber + 1
                                    var reportedIntervalLong: Long
                                    if (intervalParts.size == 2) {
                                        val intervalDouble =
                                            intervalParts[0].toDoubleOrNull() ?: 0.0
                                        reportedIntervalLong = intervalDouble.roundToLong()
                                        if (!currentIperf3RunningState.omitted) {
                                            if (reportedIntervalLong == currentIperf3RunningState.intervalNumber) {
                                                currentIperf3RunningState.messages.add("iperf3 has stalled.  Last reported interval is $reportedIntervalLong!")
                                            } else if (reportedIntervalLong != expectedIntervalLong) {
                                                currentIperf3RunningState.messages.add("Missed interval results: Expected $expectedIntervalLong but received report for $reportedIntervalLong!")
                                            } else {
                                                currentIperf3RunningState.intervalNumber =
                                                    reportedIntervalLong
                                            }
                                            if (reportedIntervalLong <= 0) {
                                                currentIperf3RunningState.messages.add("Reported output is missing the required interval number: \"$cleanLine\"!")
                                                currentIperf3RunningState.intervalNumber =
                                                    expectedIntervalLong
                                            }
                                        }
                                        currentIperf3RunningState.intervalNumber = reportedIntervalLong
                                    }
                                    currentIperf3RunningState.stalled = bitRateValue <= 0.0
                                    currentIperf3RunningState.omitted = false
                                    historicalResults.add(fromHumanUnit(currentIperf3RunningState.currentBandWidth.value, currentIperf3RunningState.currentBandWidth.unit))
                                    updateRunningAverage(historicalResults)
                                    updateRunningMedian(historicalResults)
                                    updateMax(bitRateValue, bitRateUnitString)
                                    updateMin(bitRateValue, bitRateUnitString)
                                    updateRunningDeviation(historicalResults)
                                    currentIperf3RunningState.totalSamples++
                                    if (currentIperf3RunningState.stalled) {
                                        timeLabel = "Stalled"
                                        currentIperf3RunningState.consecutiveStalled++
                                        currentIperf3RunningState.totalStalled++
                                        currentIperf3RunningState.basicBandWidthString = "${currentIperf3RunningState.basicBandWidthString} (stalled)"
                                    } else {
                                        currentIperf3RunningState.consecutiveStalled = 0
                                        timeLabel =
                                            if (!isSingleThread) formatString("%2d %s", currentIperf3RunningState.parallelStreams, "streams")
                                            else "Running"
                                    }

                                }
                            }
                            currentIperf3RunningState.formattedOutputLine =
                                formatString("%-12.12s %4.4s %-9.9s %10.10s",
                                    intervalString,
                                    bitRateString.trim(),
                                    bitRateUnitString,
                                    timeLabel)
                            currentIperf3RunningState.debugFormattedOutputLine =
                                formatString("%-12.12s %4.4s %-9.9s %7.7s %7.7s %7.7s %10.10s",
                                        intervalString,
                                        bitRateString.trim(),
                                        bitRateUnitString,
                                        toMbs(currentIperf3RunningState.avgRawBitsPerSec),
                                        toMbs(currentIperf3RunningState.minRawBitsPerSec),
                                        toMbs(currentIperf3RunningState.maxRawBitsPerSec),
                                        timeLabel)




                        }
                    }
                }
            }
        } else if (!cleanLine.startsWith("- -") && cleanLine.trim().isNotEmpty()) {
            currentIperf3RunningState.messages.add(cleanLine)
        }
        return currentIperf3RunningState
    }


    // -- private helpers --
    private fun updateMax(value: Double, unit: String) {
        val rawValue = fromHumanUnit(value, unit)
        if (rawValue > currentIperf3RunningState.maxRawBitsPerSec) {
            currentIperf3RunningState.maxRawBitsPerSec = truncateDouble(rawValue)
            currentIperf3RunningState.currentMax = toHumanUnit(rawValue)
        }
    }

    private fun updateMin(value: Double, unit: String) {
        val rawValue = fromHumanUnit(value, unit)
        if (rawValue < currentIperf3RunningState.minRawBitsPerSec) {
            currentIperf3RunningState.minRawBitsPerSec = truncateDouble(rawValue)
            currentIperf3RunningState.currentMin = toHumanUnit(rawValue)
        }
    }

    private fun updateRunningAverage(historicalResults: List<Double>) {
        if (historicalResults.isEmpty()) return
        var sum = 0.0
        for (i in historicalResults.indices) {
            sum += historicalResults[i]
        }
        val avg: Double = sum / historicalResults.size.toDouble()
        currentIperf3RunningState.currentAvg = toHumanUnit(avg)
        currentIperf3RunningState.avgRawBitsPerSec = truncateDouble(avg)
    }

    private fun updateRunningMedian(historicalResults: List<Double>) {
        val median: Double
        if (historicalResults.isEmpty()) return
        val sortedResults = historicalResults.sorted()
        val mid = sortedResults.size / 2
        median = if (sortedResults.size % 2 == 0) {
            (sortedResults[mid - 1] + sortedResults[mid]) / 2.0
        } else {
            sortedResults[mid]
        }
        currentIperf3RunningState.currentMedian = toHumanUnit(median)
        currentIperf3RunningState.medianRawBitsPerSec = truncateDouble(median)
    }

    fun updateRunningDeviation(historicalResults: List<Double>) {
        val mean = historicalResults.average()
        val variance = historicalResults.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)
        currentIperf3RunningState.currentStandardDeviation = toHumanUnit(stdDev)
        currentIperf3RunningState.standardDeviationRawBitsPerSec = truncateDouble(stdDev)
    }

    fun getCurrentLineResult() = currentIperf3RunningState
}

fun getHeading(preview: Boolean = false): String {
    return if (!preview) {
        formatString(
            "%-12.12s %4.4s %-9.9s %10.10s",
            "Interval",
            "rate",
            "Unit",
            "comment")
    } else {
        "Interval    rate Unit       comment"
    }
}

fun getDebugHeading(): String =
     formatString("%-12.12s %4.4s %-9.9s %7.7s %7.7s %7.7s %10.10s",
        "Interval",
        "rate",
        "Unit",
        "Avg",
        "Min",
        "Max",
        "comment")



fun getSampleSize(iperf3RunningState: Iperf3RunningState): String {
    var res = ""
    if (iperf3RunningState.totalSamples > 0) {
        res = formatString("Samples: %7d    [%d omitted]",
            iperf3RunningState.totalSamples,
            iperf3RunningState.totalOmitted)

        if (iperf3RunningState.totalStalled > 0) {
            res += formatString("  {%d stalled}", iperf3RunningState.totalStalled)
        }
    }
    return res
}

fun getMaximum(iperf3RunningState: Iperf3RunningState): String = if (iperf3RunningState.maxRawBitsPerSec > Double.MIN_VALUE)           "Maximum: ${toWholeNumber(iperf3RunningState.currentMax)}" else ""
fun getMinimum(resultDataDataInProgress: Iperf3RunningState): String = if (resultDataDataInProgress.minRawBitsPerSec < Double.MAX_VALUE)   "Minimum: ${toWholeNumber(resultDataDataInProgress.currentMin)}" else ""
fun getAverage(iperf3RunningState: Iperf3RunningState): String = if (iperf3RunningState.avgRawBitsPerSec >= 0)                         "Average: ${toWholeNumber(iperf3RunningState.currentAvg)}" else ""
fun getMedian(iperf3RunningState: Iperf3RunningState): String = if (iperf3RunningState.medianRawBitsPerSec >= 0)                       " Median: ${toWholeNumber(iperf3RunningState.currentMedian)}" else ""
fun getStandardDeviation(iperf3RunningState: Iperf3RunningState): String = if (iperf3RunningState.standardDeviationRawBitsPerSec >= 0) "Std Dev: ${toWholeNumber(iperf3RunningState.currentStandardDeviation)}" else ""
fun getSource(iperf3RunningState: Iperf3RunningState): String = iperf3RunningState.localHostDetails
fun getDest(iperf3RunningState: Iperf3RunningState): String = iperf3RunningState.remoteHostDetails
fun printLineResult(iperf3RunningState: Iperf3RunningState): String {
    val out = StringBuilder()
    out.append("LineResult\n ")
    iperf3RunningState.messages.indices.forEach {
        val m = iperf3RunningState.messages[it]
        out.append("             messages[$it] = $m\n ")
    }
    out.append("        Local Host: ${iperf3RunningState.localHost}\n")
    out.append("       Remote Host: ${iperf3RunningState.remoteHost}\n")
    out.append("        Local Port: ${iperf3RunningState.localPort}\n")
    out.append("       Remote Port: ${iperf3RunningState.remotePort}\n")
    out.append("       Last Result: ${iperf3RunningState.lastResult}\n")
    out.append("      Result Entry: ${iperf3RunningState.intervalNumber}\n")
    out.append(" Current Bandwidth: ${iperf3RunningState.currentBandWidth}\n")
    out.append("       Current Max: ${iperf3RunningState.currentMax}\n")
    out.append("       Current Min: ${iperf3RunningState.currentMin}\n")
    out.append("       Current Avg: ${iperf3RunningState.currentAvg}\n")
    out.append("   Basic Bandwidth: ${iperf3RunningState.basicBandWidthString}\n")
    out.append("    Formatted Line: ${iperf3RunningState.formattedOutputLine}\n")
    out.append("  Connected String: ${iperf3RunningState.connectedString}\n")
    out.append("           Timeout: ${iperf3RunningState.timeout}\n")
    out.append("  Max Raw Bits/sec: ${iperf3RunningState.maxRawBitsPerSec}\n")
    out.append("  Min Raw Bits/sec: ${iperf3RunningState.minRawBitsPerSec}\n")
    out.append("  Avg Raw Bits/sec: ${iperf3RunningState.avgRawBitsPerSec}\n")
    out.append("  Cur Raw Bits/sec: ${iperf3RunningState.currentRawBitsPerSec}\n")
    return out.toString()
}
