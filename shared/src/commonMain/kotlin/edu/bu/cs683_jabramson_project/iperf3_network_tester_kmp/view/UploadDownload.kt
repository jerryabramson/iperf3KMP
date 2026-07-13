package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view


//import android.R.attr.onClick
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton


import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiInputData
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiExecutionData
//import java.util.Locale.getDefault
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UploadDownload.DOWNLOAD
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UploadDownload.UPLOAD
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UploadDownload.isDownload


/**
 * Upload/Download radio buttons for the UI
 * @param viewModel the view model for the UI
 */
@Preview("Upload/Download Radio Button", showBackground = true, device = "id:pixel_9",
    showSystemUi = true)
@Composable
fun UploadDownload(
    inputData: UiInputData = getSampleInputData(),
    uiState: UiExecutionData = getSampleUiState(),
    setUploadDownload: (String) -> Unit = {})
{
    val color = if (uiState.isRunning) RadioButtonDefaults.colors().disabledSelectedColor else RadioButtonDefaults.colors().selectedColor
    val isReverse = inputData.isReverse
    val selected = if (isReverse) "Download" else "Upload"

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.selectableGroup().wrapContentWidth()
    )
    {
        listOf(DOWNLOAD, UPLOAD).forEach { text ->
            SelectableOption(
                enabled = !uiState.isRunning,
                selected = inputData.isReverse == isDownload(text),
                onClick = { if (!uiState.isRunning) setUploadDownload(text) }
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }
    }
}

@Composable
fun SelectableOption(enabled: Boolean = true, selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    val selectedColor = if (!enabled) RadioButtonDefaults.colors().disabledSelectedColor else RadioButtonDefaults.colors().selectedColor
    val unSelectedColor = if (!enabled) RadioButtonDefaults.colors().disabledUnselectedColor else RadioButtonDefaults.colors().unselectedColor
    Row(
        Modifier.selectable(
            selected = selected,
            onClick = onClick,
            role = Role.Checkbox
        ),
        verticalAlignment = Alignment.CenterVertically,
        ) {
        RadioButton(selected = selected,
            onClick = onClick,
            modifier = Modifier.height(25.dp),
            colors = if (selected) RadioButtonDefaults.colors(selectedColor = selectedColor) else RadioButtonDefaults.colors(unselectedColor = unSelectedColor)
        )
        content()
    }
}
