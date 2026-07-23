package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view

//import android.R
//import android.util.Log.e
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData
//import java.nio.file.Files.size


/**
 * Bottom bar for the UI.
 */
@Composable
@Preview(name ="Project Bottom Bar", showBackground = true, showSystemUi = true)
fun ProjectBottomBar(currentDebugging: Boolean = getSampleInputData().isDebugging,
                     isCompact: Boolean = false,
                     toggleDebug: () -> Unit = {}
) {
    val fontSize = 10.sp
    val style = mesloMonoTextStyle().copy(fontSize = fontSize)

    Row(verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(start = 2.dp, end =2.dp)
            .fillMaxWidth()) {
        // There isn't room for the course/author footer alongside the debug
        // toggle when vertical space is tight (e.g. a phone in landscape).
        //if (!isCompact) {
            Column(modifier = Modifier.padding(end = 2.dp)) {
                Text(
                    text = if (isCompact) "Mobile Development Directed Study - Jerold Abramson" else "Mobile Development Directed Study",
                    style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge
                )
                if (!isCompact) {
                    Text(
                        text = "Jerold Abramson",
                        style = style,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        //}
        DebugOnOffToggle(currentDebugging, toggleDebug, isCompact)
    }
}