package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.model

//import androidx.room.Entity
//import androidx.room.PrimaryKey
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.UnitConvertedData
//import java.util.UUID

//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.UuidTypeConverter
//import androidx.room.TypeConverters
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.tobps
//import java.time.LocalDateTime
//import java.time.OffsetDateTime


//@Entity(tableName="result_data") @TypeConverters(UuidTypeConverter::class)
data class ResultData(

    // Record creation details
    //@PrimaryKey
//    val guid: UUID,
    //val startTimeMillis: Long,
    //val endTimeMillis: Long,
//    val startDateTime: LocalDateTime,
//    val endDateTime: LocalDateTime,


    // Input Options for test
    val direction: String,
    val duration: Int,
    val parallelStreams: Int,
    val skip: Int,

    // connection details
    val localHostDetails: String,
    val remoteHostDetails: String,

    // runtime results
    val reportedOmitted: Long,
    val reportedIterations: Long,
    val max : UnitConvertedData,
    val min: UnitConvertedData,
    val avg: UnitConvertedData,
    val median: UnitConvertedData,
    val standardDeviation: UnitConvertedData,

    val maxbps: Long,
    val minbps: Long,
    val avgbps: Long,
    val medianbps: Long,
    val stdDevbps: Long,
)

/*
This is the data entity class.
User @Entity annotation
 */

data class ResultDataInProgress(

    // db keys
    //val guid: UUID = UUID.randomUUID(),
    //val startTimeMillis: Long = System.currentTimeMillis(),

    // test input details
    val isReverse: Boolean = false,
    val duration: Int = -1,
    val parallelStreams: Int = -1,
    val skip: Int = -1,

    //val startDateTime: LocalDateTime = OffsetDateTime.now().toLocalDateTime(),
    var localHost: String = "",
    var remoteHost: String = "",
    var localPort: Long = -1L,
    var remotePort: Long = -1L,
    var localHostDetails: String = "",
    var rawLocalHostDetails: String = "",
    var remoteHostDetails: String = "",
    var rawRemoteHostDetails: String = "",

    // iperf3 messages
    var messages: MutableList<String> = emptyList<String>().toMutableList(),

    // interval number (0 to duration, can go backwards with Omit)
    var intervalNumber: Long = -1,
    var totalSamples: Long = -1,
    var totalOmitted: Long = 0,

    // statistics - converted to human-readable units
    var currentBandWidth : UnitConvertedData = UnitConvertedData(),
    var currentMax: UnitConvertedData = UnitConvertedData(),
    var currentMin: UnitConvertedData = UnitConvertedData(),
    var currentAvg: UnitConvertedData = UnitConvertedData(),
    var currentMedian: UnitConvertedData = UnitConvertedData(),
    var currentStandardDeviation: UnitConvertedData = UnitConvertedData(),

    // processed output from iperf3
    var basicBandWidthString: String = "",
    var debugFormattedOutputLine: String = "",
    var formattedOutputLine: String = "",
    var connectedString: String = "",
    var timeout: String = "",
    var rawAverage: String = "",
    var rawOutputLine: String = "",
    var omitted: Boolean = false,
    var stalled: Boolean = false,
    var consecutiveStalled: Int = 0,
    var totalStalled: Int = 0,


    // statistics - raw numeric values
    var maxRawBitsPerSec: Double = Double.MIN_VALUE,
    var minRawBitsPerSec: Double = Double.MAX_VALUE,
    var avgRawBitsPerSec: Double = 0.toDouble(),
    var medianRawBitsPerSec: Double = 0.toDouble(),
    var currentRawBitsPerSec: Double = 0.toDouble(),
    var standardDeviationRawBitsPerSec: Double = 0.toDouble(),

    var lastResult: String = "",
)

fun createResultData(resultDataInProgress: ResultDataInProgress): ResultData =
    ResultData(
        // keys
        //guid = resultDataInProgress.guid,
        //startTimeMillis = resultDataInProgress.startTimeMillis,
        //startDateTime = resultDataInProgress.startDateTime,
        //endDateTime = LocalDateTime.now(),

        // Input Params
        direction = if (resultDataInProgress.isReverse) "Download" else "Upload",
        duration =  resultDataInProgress.duration,
        parallelStreams = resultDataInProgress.parallelStreams,
        skip =  resultDataInProgress.skip,

        // final runtime results
        localHostDetails = resultDataInProgress.rawLocalHostDetails,
        remoteHostDetails = resultDataInProgress.rawRemoteHostDetails,
        reportedOmitted = resultDataInProgress.totalOmitted,
        reportedIterations = resultDataInProgress.totalOmitted + resultDataInProgress.totalSamples,
        //endTimeMillis = System.currentTimeMillis(),

        // human-readable results
        max = resultDataInProgress.currentMax,
        min = resultDataInProgress.currentMin,
        avg = resultDataInProgress.currentAvg,
        median = resultDataInProgress.currentMedian,
        standardDeviation = resultDataInProgress.currentStandardDeviation,

        // raw bps results
        maxbps = tobps(resultDataInProgress.maxRawBitsPerSec),
        minbps = tobps(resultDataInProgress.minRawBitsPerSec),
        avgbps = tobps(resultDataInProgress.avgRawBitsPerSec),
        stdDevbps = tobps(resultDataInProgress.standardDeviationRawBitsPerSec),
        medianbps = tobps(resultDataInProgress.medianRawBitsPerSec),
    )
