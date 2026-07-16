package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view

import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.model.ResultDataInProgress
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.UnitConvertedData
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.fromHumanUnit
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.DefaultInputData
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiExecutionData
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData

class SampleUIData {
    val sampleOutputData = listOf(
        " 0.00-1.00  2.35 Gbits/sec  skipped",
        " 1.00-2.00  2.35 Gbits/sec  skipped",
        " 0.00-1.00  2.35 Gbits/sec  8 streams",
        " 1.00-2.00  2.36 Gbits/sec  8 streams",
        " 2.00-3.00  2.35 Gbits/sec  8 streams",
        " 3.00-4.00  2.35 Gbits/sec  8 streams",
        " 4.00-5.00  2.35 Gbits/sec  8 streams",
        " 5.00-6.00  2.35 Gbits/sec  8 streams",
        " 6.00-7.00  2.35 Gbits/sec  8 streams",
        " 7.00-8.00  2.35 Gbits/sec  8 streams",
        " 8.00-9.00  2.35 Gbits/sec  8 streams",
        " 9.00-10.00 2.35 Gbits/sec  8 streams",
        "10.00-11.00 2.35 Gbits/sec  8 streams",
        "11.00-12.00 2.35 Gbits/sec  8 streams",
        "12.00-13.00 2.35 Gbits/sec  8 streams",
        "13.00-14.00 2.35 Gbits/sec  8 streams",
        "14.00-15.00 2.36 Gbits/sec  8 streams",
        "15.00-16.00 2.35 Gbits/sec  8 streams",
        "16.00-17.00 2.35 Gbits/sec  8 streams",
        "17.00-18.00 2.35 Gbits/sec  8 streams",
        "18.00-19.00 2.35 Gbits/sec  8 streams",
        "19.00-20.00 2.35 Gbits/sec  8 streams",
        "20.00-21.00 2.36 Gbits/sec  8 streams",
        "21.00-22.00 2.35 Gbits/sec  8 streams",
        "22.00-23.00 2.36 Gbits/sec  8 streams",
        "23.00-24.00 2.35 Gbits/sec  8 streams",
        "24.00-25.00 2.35 Gbits/sec  8 streams",
        "25.00-26.00 2.35 Gbits/sec  8 streams",
        "26.00-27.00 2.35 Gbits/sec  8 streams",
        "27.00-28.00 2.35 Gbits/sec  8 streams",
        "28.00-29.00 2.36 Gbits/sec  8 streams",
        "29.00-30.00 2.35 Gbits/sec  8 streams",
        ).toMutableList()



    val localHostPort  = "10.0.0.2.16:51088"
    val remoteHostPort = "192.168.1.32:5201"

    val sampleIperf3Messages = listOf(
        "{iPerf JNI} 🚀 Initiating iPerf3 client request ...",
        "Connecting to host jabramson.com, port 5201",
        "Reverse mode, remote host 192.168.127.12 is sending",
        " Local Host:port $localHostPort",
        "Remote Host:port $remoteHostPort",
        "iperf Done.",
        "{iPerf JNI} 🚀 Test completed successfully."
    ).toMutableList()

    val sampleStatistics = listOf(
        "Samples:       10   [2 Omitted]",
        "Average:      996.00 Mbits/sec",
        "Maximum:        1.09 Gbits/sec",
        "Minimum:      838.00 Mbits/sec",
        " Median:      981.00 Mbits/sec",
        "Std Dev:      364.72 Mbits/sec",
    ).toMutableList()

    val sampleMin: UnitConvertedData = UnitConvertedData(52.30, "Mbits/sec")
    val sampleMax: UnitConvertedData = UnitConvertedData(163.00, "Mbits/sec")
    val sampleAvg: UnitConvertedData = UnitConvertedData(140.00, "Mbits/sec")
    val sampleMedian: UnitConvertedData = UnitConvertedData(153.00, "Mbits/sec")
    val sampleStdDev = UnitConvertedData(33.33, "Mbits/sec")
    val sampleResultData = ResultDataInProgress(
        totalSamples = 10,
        totalOmitted = 2,
        currentMax = sampleMax,
        currentAvg = sampleAvg,
        currentMin = sampleMin,
        currentMedian = sampleMedian,
        currentStandardDeviation = sampleStdDev,
        maxRawBitsPerSec = fromHumanUnit(sampleMax),
        minRawBitsPerSec = fromHumanUnit(sampleMin),
        medianRawBitsPerSec = fromHumanUnit(sampleMedian),
        avgRawBitsPerSec = fromHumanUnit(sampleAvg),
        standardDeviationRawBitsPerSec = fromHumanUnit(sampleStdDev),
        intervalNumber = 1,
        basicBandWidthString = "191 Mbits/sec",
        localHostDetails = " Local Host:port $localHostPort",
        remoteHostDetails = "Remote Host:port $remoteHostPort",
        remotePort = 5201,
        messages = mutableListOf<String>()
    )

    val sampleErrorLines = emptyList<String>().toMutableList()
    //    "Error: Failed to bind to 192.168.1.1:5201: Address already in use",
    //.toMutableList()


    val sampleUiStateExampleRunning = UiExecutionData(
        isRunning = true,
        isFinished = false,
        iperf3Messages = sampleIperf3Messages,
        bandWidth = "1.00-2.00    35.6 Mbits/sec",
        progress = 0.33.toFloat(),
        results = sampleStatistics,
        outputLines = sampleOutputData,
        resultDataInProgress = sampleResultData,
        //errorLines = sampleErrorLines,
        latestLine = "some stuff",
        returnCode = 0
    )

    val sampleUiStateExampleFinished = UiExecutionData(
        isRunning = false,
        bandWidth = "1.00-2.00    35.6 Mbits/sec",
        isFinished = true,
        results = sampleStatistics,
        outputLines = sampleOutputData,
        resultDataInProgress = sampleResultData,
        errorLines = sampleErrorLines,
        latestLine = "some stuff",
        returnCode = 0,
        iperf3Messages = sampleIperf3Messages
    )

    val sampleInputs = UiInputData(
        hostName = DefaultInputData.HOST_NAME,
        hostField = DefaultInputData.HOST_FIELD,
        durationSecs = 10,
        parallelStreams = 8,
        skip = 2,
        portNumber = DefaultInputData.PORT_NUMBER,
        isDebugging = true,
        isReverse = false,
        isForceFlush = false,
    )

}

fun getSampleUiState() = SampleUIData().sampleUiStateExampleRunning
fun getSampleInputData() = SampleUIData().sampleInputs
fun getSampleUiState(isRunning: Boolean = true) =
    if (isRunning) SampleUIData().sampleUiStateExampleRunning
    else SampleUIData().sampleUiStateExampleFinished

