
package edu.bu.cs683_jabramson_project.iperf3_network_tester.view



import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.ui.res.painterResource


import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf

import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp






// ======================
// HELPER: DIRECTIONAL PROGRESS INDICATOR
// ======================

@Preview(name = "Network Progress Indicator Preview", showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun NetworkProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float = 0.5.toFloat(),
    isDownload: Boolean = true, // true = download (data IN), false = upload (data OUT)
    arrowSize: Dp = 20.dp,
    progressBarHeight: Dp = 4.dp,
    trackColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    arrowColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var widthDp by remember { mutableFloatStateOf(0f) } // Store width in dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(progressBarHeight)
    ) {
        // Base progress indicator (direction depends on data flow)
        if (isDownload) {
            // DOWNLOAD: Data flows NETWORK → DEVICE (right → left)
            // Visual: Filled portion grows FROM RIGHT TO LEFT
            ReverseLinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // UPLOAD: Data flows DEVICE → NETWORK (left → right)
            // Visual: Filled portion grows FROM LEFT TO RIGHT
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = trackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }

        // Directional arrow positioned at the progress point
        val arrowCenterX =
            if (isDownload)
                widthDp * (1 - progress) // Download: arrow moves RIGHT→LEFT
            else
                widthDp * progress       // Upload: arrow moves LEFT→RIGHT

        // Clamp arrow position to prevent clipping (all calculations in dp)
        val clampedOffset = arrowCenterX.coerceIn(
            arrowSize.value / 2,
            widthDp.toInt() - (arrowSize.value / 2)
        )

        // Vertical offset to the center arrow on the progress bar
        val verticalOffset = ((progressBarHeight - arrowSize) / 2)

//        Icon(
//            painter = painterResource(R.drawable.baseline_directions_bus_24),
//            contentDescription = if (isDownload)
//                "Download progress: ${(progress * 100).toInt()}%"
//            else
//                "Upload progress: ${(progress * 100).toInt()}%",
//            modifier = Modifier
//                .size(arrowSize)
//                .offset(
//                    x = (clampedOffset - (arrowSize.value / 2)).dp, // Convert the center offset to top-left
//                    y = verticalOffset.value.dp
//                )
//                .wrapContentSize(align = Alignment.TopStart),
//            tint = arrowColor
//        )
    }
}

// ======================
// HELPER: REVERSE PROGRESS INDICATOR (for download)
// ======================
@Composable
private fun ReverseLinearProgressIndicator(
    progress: Float, // 0.0 to 1.0 (0% to 100%)
    modifier: Modifier = Modifier,
) {
    val reversedProgress = 1f - progress // Critical: Invert progress for RTL effect
    LinearProgressIndicator(
    progress = { reversedProgress },
    modifier = modifier
                .wrapContentWidth(),
    color = ProgressIndicatorDefaults.linearColor,
    trackColor = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
    )
}
