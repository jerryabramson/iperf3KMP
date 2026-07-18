package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.model

//import androidx.room.Entity
//import androidx.room.PrimaryKey
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.AppLocalDateTime
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.AppUUID
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.getAppCurrentTimeMillis
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.getAppLocalDateTime
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.getAppUUID
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.UnitConvertedData


//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.UuidTypeConverter
//import androidx.room.TypeConverters
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.utils.tobps


//@Entity(tableName="result_data") @TypeConverters(UuidTypeConverter::class)
data class ResultData(

    // Record creation details
    //@PrimaryKey
    val guid: AppUUID,

    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val startDateTime: AppLocalDateTime,
    val endDateTime: AppLocalDateTime,


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

fun createResultData(iperf3RunningState: Iperf3RunningState): ResultData =
    ResultData(
        // keys
        //guid = iperf3RunningState.guid,
        startTimeMillis = iperf3RunningState.startTimeMillis,
        endTimeMillis = getAppCurrentTimeMillis().value,
        startDateTime = iperf3RunningState.startDateTime,
        endDateTime = getAppLocalDateTime(),

        guid =  iperf3RunningState.guid,

        // Input Params
        direction = if (iperf3RunningState.isReverse) "Download" else "Upload",
        duration =  iperf3RunningState.duration,
        parallelStreams = iperf3RunningState.parallelStreams,
        skip =  iperf3RunningState.skip,

        // final runtime results
        localHostDetails = iperf3RunningState.rawLocalHostDetails,
        remoteHostDetails = iperf3RunningState.rawRemoteHostDetails,
        reportedOmitted = iperf3RunningState.totalOmitted,
        reportedIterations = iperf3RunningState.totalOmitted + iperf3RunningState.totalSamples,
        //endTimeMillis = System.currentTimeMillis(),

        // human-readable results
        max = iperf3RunningState.currentMax,
        min = iperf3RunningState.currentMin,
        avg = iperf3RunningState.currentAvg,
        median = iperf3RunningState.currentMedian,
        standardDeviation = iperf3RunningState.currentStandardDeviation,

        // raw bps results
        maxbps = tobps(iperf3RunningState.maxRawBitsPerSec),
        minbps = tobps(iperf3RunningState.minRawBitsPerSec),
        avgbps = tobps(iperf3RunningState.avgRawBitsPerSec),
        stdDevbps = tobps(iperf3RunningState.standardDeviationRawBitsPerSec),
        medianbps = tobps(iperf3RunningState.medianRawBitsPerSec)

    )
