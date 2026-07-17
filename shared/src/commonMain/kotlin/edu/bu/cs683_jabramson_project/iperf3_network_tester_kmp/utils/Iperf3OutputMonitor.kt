package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils

//import android.annotation.SuppressLint
//import androidx.compose.ui.text.intl.Locale

import androidx.compose.foundation.layout.IntrinsicSize
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.getAppUUID
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.model.ResultDataInProgress
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
    private var currentResultDataInProgress: ResultDataInProgress = ResultDataInProgress()
    private var gathered = false
    private var lastResult = ""
    private var summaryResults = false
    private var finished = false
    private var lastOmitted = false
    private var isSingleThread = true
    private var historicalResults: MutableList<Double> = emptyList<Double>().toMutableList()

    /** Reset all accumulated state for a new test run. */
   fun reset(params: UiInputData) {

        currentResultDataInProgress = ResultDataInProgress(
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


    fun processCancelLine(line: String): ResultDataInProgress {
        currentResultDataInProgress.messages.add(line)
        return currentResultDataInProgress
    }
    /** Parse one iperf3 output line and return all extracted data at once. */
    //@SuppressLint("DefaultLocale"
    fun processLine(line: String): ResultDataInProgress {
        val cleanLine = line.replace("\n", "")
        currentResultDataInProgress.rawOutputLine = cleanLine
        val firstLeftBracket = cleanLine.indexOf('[')
        val firstRightBracket = cleanLine.indexOf(']')
        if (firstLeftBracket in 0..<firstRightBracket) {
            val id = cleanLine.substring(firstLeftBracket + 1, firstRightBracket)
            val restOfLine = cleanLine.substring(firstRightBracket + 1).split(Regex("[ \\t]+"))
            // Connection-info line (e.g. "[ ID ] Interval...")
            if (id == "ID") {
                if (!gathered) {
                    gathered = true
                    currentResultDataInProgress.intervalNumber = 0
                }
            } else {
                // Pre-gathering: extract connection details or collect messages
                if (!gathered) {
                    if (restOfLine.size == 10) {
                        currentResultDataInProgress.localHost = restOfLine[2].trim()
                        currentResultDataInProgress.localPort = restOfLine[4].trim().toLongOrNull() ?: -1L
                        currentResultDataInProgress.remoteHost = restOfLine[7].trim()
                        currentResultDataInProgress.remotePort = restOfLine[9].trim().toLongOrNull() ?: -1L
                        currentResultDataInProgress.connectedString = restOfLine[5].trim()
                        currentResultDataInProgress.timeout = restOfLine[6]
                        val localHostPort =  "$currentResultDataInProgress.localHost:$currentResultDataInProgress.localPort"
                        val remoteHostPort = "$currentResultDataInProgress.remoteHost:$currentResultDataInProgress.remotePort"

                        currentResultDataInProgress.localHostDetails = "Local Host:port $localHostPort"
                        currentResultDataInProgress.remoteHostDetails = "Remote Host:port $remoteHostPort"
                        currentResultDataInProgress.rawLocalHostDetails = localHostPort
                        currentResultDataInProgress.rawRemoteHostDetails = remoteHostPort
                        currentResultDataInProgress.messages.add(" Local Host:port $localHostPort")
                        currentResultDataInProgress.messages.add("Remote Host:port $remoteHostPort")
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
                            currentResultDataInProgress.currentBandWidth = UnitConvertedData(bitRateValue, bitRateUnitString)
                            currentResultDataInProgress.basicBandWidthString = "$bitRateString $bitRateUnitString"
                            val timeLabel: String
                            when (sendOrReceive.lowercase()) {
                                "(omitted)" -> {
                                    lastOmitted = true
                                    timeLabel = "skipped"
                                    currentResultDataInProgress.totalOmitted++
                                    currentResultDataInProgress.omitted = true
                                    currentResultDataInProgress.basicBandWidthString = "${currentResultDataInProgress.basicBandWidthString} (omitted)"
                                }

                                "sender", "receiver" -> {
                                    lastOmitted = false
                                    currentResultDataInProgress.omitted = false
                                    if (!finished) {
                                        finished = true
                                        summaryResults = true
                                        currentResultDataInProgress.totalSamples++
                                        currentResultDataInProgress.currentBandWidth = UnitConvertedData(bitRateValue, bitRateString)
                                        currentResultDataInProgress.rawAverage = "$bitRateString $bitRateUnitString"
                                        currentResultDataInProgress.currentAvg = UnitConvertedData(bitRateValue, bitRateUnitString)
                                    }
                                    timeLabel = sendOrReceive //String.format("%s", sendOrReceive)
                                }
                                else -> {
                                    if (lastOmitted) currentResultDataInProgress.intervalNumber = 0
                                    lastOmitted = false
                                    val intervalParts = intervalString.split("-")
                                    val expectedIntervalLong = currentResultDataInProgress.intervalNumber + 1
                                    var reportedIntervalLong: Long
                                    if (intervalParts.size == 2) {
                                        val intervalDouble =
                                            intervalParts[0].toDoubleOrNull() ?: 0.0
                                        reportedIntervalLong = intervalDouble.roundToLong()
                                        if (!currentResultDataInProgress.omitted) {
                                            if (reportedIntervalLong == currentResultDataInProgress.intervalNumber) {
                                                currentResultDataInProgress.messages.add("iperf3 has stalled.  Last reported interval is $reportedIntervalLong!")
                                            } else if (reportedIntervalLong != expectedIntervalLong) {
                                                currentResultDataInProgress.messages.add("Missed interval results: Expected $expectedIntervalLong but received report for $reportedIntervalLong!")
                                            } else {
                                                currentResultDataInProgress.intervalNumber =
                                                    reportedIntervalLong
                                            }
                                            if (reportedIntervalLong <= 0) {
                                                currentResultDataInProgress.messages.add("Reported output is missing the required interval number: \"$cleanLine\"!")
                                                currentResultDataInProgress.intervalNumber =
                                                    expectedIntervalLong
                                            }
                                        }
                                        currentResultDataInProgress.intervalNumber = reportedIntervalLong
                                    }
                                    currentResultDataInProgress.stalled = bitRateValue <= 0.0
                                    currentResultDataInProgress.omitted = false
                                    historicalResults.add(fromHumanUnit(currentResultDataInProgress.currentBandWidth.value, currentResultDataInProgress.currentBandWidth.unit))
                                    updateRunningAverage(historicalResults)
                                    updateRunningMedian(historicalResults)
                                    updateMax(bitRateValue, bitRateUnitString)
                                    updateMin(bitRateValue, bitRateUnitString)
                                    updateRunningDeviation(historicalResults)
                                    currentResultDataInProgress.totalSamples++
                                    if (currentResultDataInProgress.stalled) {
                                        timeLabel = "Stalled"
                                        currentResultDataInProgress.consecutiveStalled++
                                        currentResultDataInProgress.totalStalled++
                                        currentResultDataInProgress.basicBandWidthString = "${currentResultDataInProgress.basicBandWidthString} (stalled)"
                                    } else {
                                        currentResultDataInProgress.consecutiveStalled = 0
                                        timeLabel =if (!isSingleThread) "$currentResultDataInProgress.parallelStreams streams" else "Running"
                                        //    if (!isSingleThread) "%2d %s".format(
                                        //    else "%s".format("Running")
                                    }

                                }
                            }
                            currentResultDataInProgress.formattedOutputLine =
                                //String.format("%-12.12s %4.4s %-9.9s %10.10s",
                                    "$intervalString ${bitRateString.trim()}  $bitRateUnitString $timeLabel"
                            currentResultDataInProgress.debugFormattedOutputLine =
                            //    String.format("%-12.12s %4.4s %-9.9s %7.7s %7.7s %7.7s %10.10s",
                                    """
                                        $intervalString 
                                        $bitRateString.trim() 
                                        $bitRateUnitString 
                                        ${toMbs(currentResultDataInProgress.avgRawBitsPerSec)} 
                                        ${toMbs(currentResultDataInProgress.minRawBitsPerSec)}  
                                        ${toMbs(currentResultDataInProgress.maxRawBitsPerSec)}
                                        $timeLabel
                                    """



                        }
                    }
                }
            }
        } else if (!cleanLine.startsWith("- -") && cleanLine.trim().isNotEmpty()) {
            currentResultDataInProgress.messages.add(cleanLine)
        }
        return currentResultDataInProgress
    }


    // -- private helpers --
    private fun updateMax(value: Double, unit: String) {
        val rawValue = fromHumanUnit(value, unit)
        if (rawValue > currentResultDataInProgress.maxRawBitsPerSec) {
            currentResultDataInProgress.maxRawBitsPerSec = truncateDouble(rawValue)
            currentResultDataInProgress.currentMax = toHumanUnit(rawValue)
        }
    }

    private fun updateMin(value: Double, unit: String) {
        val rawValue = fromHumanUnit(value, unit)
        if (rawValue < currentResultDataInProgress.minRawBitsPerSec) {
            currentResultDataInProgress.minRawBitsPerSec = truncateDouble(rawValue)
            currentResultDataInProgress.currentMin = toHumanUnit(rawValue)
        }
    }

    private fun updateRunningAverage(historicalResults: List<Double>) {
        if (historicalResults.isEmpty()) return
        var sum = 0.0
        for (i in historicalResults.indices) {
            sum += historicalResults[i]
        }
        val avg: Double = sum / historicalResults.size.toDouble()
        currentResultDataInProgress.currentAvg = toHumanUnit(avg)
        currentResultDataInProgress.avgRawBitsPerSec = truncateDouble(avg)
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
        currentResultDataInProgress.currentMedian = toHumanUnit(median)
        currentResultDataInProgress.medianRawBitsPerSec = truncateDouble(median)
    }

    fun updateRunningDeviation(historicalResults: List<Double>) {
        val mean = historicalResults.average()
        val variance = historicalResults.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)
        currentResultDataInProgress.currentStandardDeviation = toHumanUnit(stdDev)
        currentResultDataInProgress.standardDeviationRawBitsPerSec = truncateDouble(stdDev)
    }

    fun getCurrentLineResult() = currentResultDataInProgress
}

fun getHeading(): String {
    return "Interval    rate Unit         comment"
}



fun getSampleSize(resultDataInProgress: ResultDataInProgress): String {
    var res = ""
    if (resultDataInProgress.totalSamples > 0) {
        res = "Samples: ${resultDataInProgress.totalSamples} [${resultDataInProgress.totalOmitted} omitted]"
            //Locale.US,
        //    resultDataInProgress.totalSamples,
        //    ,

        //)
        if (resultDataInProgress.totalStalled > 0) {
            res += "  {$resultDataInProgress.totalStalled stalled}"
        }
    }
    return res
}

fun getMaximum(resultDataInProgress: ResultDataInProgress): String = if (resultDataInProgress.maxRawBitsPerSec > Double.MIN_VALUE)           "Maximum: ${toWholeNumber(resultDataInProgress.currentMax)}" else ""
fun getMinimum(resultDataDataInProgress: ResultDataInProgress): String = if (resultDataDataInProgress.minRawBitsPerSec < Double.MAX_VALUE)   "Minimum: ${toWholeNumber(resultDataDataInProgress.currentMin)}" else ""
fun getAverage(resultDataInProgress: ResultDataInProgress): String = if (resultDataInProgress.avgRawBitsPerSec >= 0)                         "Average: ${toWholeNumber(resultDataInProgress.currentAvg)}" else ""
fun getMedian(resultDataInProgress: ResultDataInProgress): String = if (resultDataInProgress.medianRawBitsPerSec >= 0)                       " Median: ${toWholeNumber(resultDataInProgress.currentMedian)}" else ""
fun getStandardDeviation(resultDataInProgress: ResultDataInProgress): String = if (resultDataInProgress.standardDeviationRawBitsPerSec >= 0) "Std Dev: ${toWholeNumber(resultDataInProgress.currentStandardDeviation)}" else ""
fun getSource(resultDataInProgress: ResultDataInProgress): String = resultDataInProgress.localHostDetails
fun getDest(resultDataInProgress: ResultDataInProgress): String = resultDataInProgress.remoteHostDetails
fun printLineResult(resultDataInProgress: ResultDataInProgress): String {
    val out = StringBuilder()
    out.append("LineResult\n ")
    resultDataInProgress.messages.indices.forEach {
        val m = resultDataInProgress.messages[it]
        out.append("             messages[$it] = $m\n ")
    }
    out.append("        Local Host: ${resultDataInProgress.localHost}\n")
    out.append("       Remote Host: ${resultDataInProgress.remoteHost}\n")
    out.append("        Local Port: ${resultDataInProgress.localPort}\n")
    out.append("       Remote Port: ${resultDataInProgress.remotePort}\n")
    out.append("       Last Result: ${resultDataInProgress.lastResult}\n")
    out.append("      Result Entry: ${resultDataInProgress.intervalNumber}\n")
    out.append(" Current Bandwidth: ${resultDataInProgress.currentBandWidth}\n")
    out.append("       Current Max: ${resultDataInProgress.currentMax}\n")
    out.append("       Current Min: ${resultDataInProgress.currentMin}\n")
    out.append("       Current Avg: ${resultDataInProgress.currentAvg}\n")
    out.append("   Basic Bandwidth: ${resultDataInProgress.basicBandWidthString}\n")
    out.append("    Formatted Line: ${resultDataInProgress.formattedOutputLine}\n")
    out.append("  Connected String: ${resultDataInProgress.connectedString}\n")
    out.append("           Timeout: ${resultDataInProgress.timeout}\n")
    out.append("       Raw Average: ${resultDataInProgress.rawAverage}\n")
    out.append("  Max Raw Bits/sec: ${resultDataInProgress.maxRawBitsPerSec}\n")
    out.append("  Min Raw Bits/sec: ${resultDataInProgress.minRawBitsPerSec}\n")
    out.append("  Avg Raw Bits/sec: ${resultDataInProgress.avgRawBitsPerSec}\n")
    out.append("  Cur Raw Bits/sec: ${resultDataInProgress.currentRawBitsPerSec}\n")
    return out.toString()
}
