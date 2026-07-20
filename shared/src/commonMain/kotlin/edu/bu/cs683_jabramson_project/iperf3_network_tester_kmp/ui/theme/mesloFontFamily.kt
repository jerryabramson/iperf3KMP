package edu.bu.cs683_jabramson_project.iperf3_network_tester_kmp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import iperf3kmp.shared.generated.resources.Res
import iperf3kmp.shared.generated.resources.meslonerdfont
import org.jetbrains.compose.resources.Font


@Composable
fun mesloFontFamily(): FontFamily = FontFamily(Font(Res.font.meslonerdfont))
