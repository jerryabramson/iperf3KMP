package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.ui.theme.mesloFontFamily

@Composable
fun mesloMonoTextStyle(): TextStyle = TextStyle(
    fontFamily = mesloFontFamily(),
    fontSize = 14.sp,
    letterSpacing = 0.2.sp,
    color = MaterialTheme.colorScheme.onSurface
)
