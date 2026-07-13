package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
val arrow_forward: ImageVector
    get() {
        if (arrowForward != null) {
            return arrowForward!!
        }
        arrowForward =
            ImageVector.Builder(
                name = "arrow_forward",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(16.18f, 13f)
                        horizontalLineTo(4f)
                        verticalLineTo(11f)
                        horizontalLineTo(16.18f)
                        lineTo(10.58f, 5.4f)
                        lineTo(12f, 4f)
                        lineToRelative(8f, 8f)
                        lineToRelative(-8f, 8f)
                        lineTo(10.58f, 18.6f)
                        lineTo(16.18f, 13f)
                        close()
                    }
                }
                .build()
        return arrowForward!!
    }

private var arrowForward: ImageVector? = null