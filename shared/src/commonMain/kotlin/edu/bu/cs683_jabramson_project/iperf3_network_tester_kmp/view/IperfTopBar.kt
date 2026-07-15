package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.view

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
//import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.R
import edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.viewmodel.UiExecutionData

/**
 * Top bar for the UI.
 * @param buttonAction the action to be performed when the button is clicked
 * @uiState the current state of the UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name ="Iperf Top Bar",showSystemUi = true)
@Composable
fun IperfTopBar(
    currentRunning: Boolean = false,
    currentFinished: Boolean = false,
    currentSaved: Boolean = false,
    buttonAction: () -> Unit = {},
    saveButtonAction: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),

        navigationIcon = {
            SaveButton(isRunning = currentRunning,
                isFinished = currentFinished,
                isSaved = currentSaved,
                saveButtonAction = saveButtonAction)
        },
        title = {
            Row(
                modifier = Modifier
                    //.background(color = MaterialTheme.colorScheme.onPrimaryContainer)
                    //.padding(start = 20.dp, end = 20.dp),
            )
            {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    text =  "iperf3 Network Tester",
                    //text = stringResource(
                    //    id = Res.string.app_name,
                    //    resource = TODO()
                    //),
                    //color = MaterialTheme.colorScheme.surface
                )
            }
        },
        actions = {
            RunButton(
                isRunning = currentRunning,
                buttonAction = buttonAction
            )
        }


    )
}