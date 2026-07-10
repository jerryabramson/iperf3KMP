package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.ResultData
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.ResultDataInProgress
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.UnitConvertedData
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.DefaultInputData
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiExecutionData
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiInputData

class SampleUIData {
    val sampleOutputData = listOf(
        "0.00-1.00   35.6 Mbits/sec Skipped",
        "1.00-2.00   35.6 Mbits/sec Skipped",
        "2.00-3.00   35.6 Mbits/sec 8 Streams ",
        "...",
        "...",
        "...",
        "...",
        "0.00-10.19   24.0 Mbits/sec sender",
        "0.00-10.19   24.0 Mbits/sec receiver",

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

    val sampleResultData = ResultDataInProgress(
        totalSamples = 10,
        totalOmitted = 2,
        currentMax = UnitConvertedData(170.00, "Mbits/sec"),
        currentAvg = UnitConvertedData(164.81, "Mbits/sec"),
        currentMin = UnitConvertedData(81.20, "Mbits/sec"),
        currentMedian = UnitConvertedData(121.60, "Mbits/sec"),
        intervalNumber = 1,
        basicBandWidthString = "191 Mbits/sec",
        localHostDetails = " Local Host:port $localHostPort",
        remoteHostDetails = "Remote Host:port $remoteHostPort",
        remotePort = 5201,
        messages = mutableListOf<String>()
    )

    val sampleErrorLines = listOf(
        "Error: Failed to bind to 192.168.1.1:5201: Address already in use",
    ).toMutableList()


    val sampleUiStateExample = UiExecutionData(
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

    val sampleInputs = UiInputData(
        hostName = "", //DefaultInputData.HOST_NAME,
        hostField ="", // DefaultInputData.HOST_FIELD,

        //durationSecs = 10,
        //parallelStreams = 8,
        //skip = 2,
        //portNumber = DefaultInputData.PORT_NUMBER,
        isDebugging = true,
        isReverse = false,
        isForceFlush = false,
    )

}

fun getSampleUiState() = SampleUIData().sampleUiStateExample
fun getSampleInputData() = SampleUIData().sampleInputs
