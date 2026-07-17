package com.nova.assistant.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nova.assistant.BuildConfig
import com.nova.assistant.util.BatteryOptimizationUtil
import com.nova.assistant.util.Constants
import com.nova.assistant.presentation.ui.theme.*
import com.nova.assistant.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val batteryUtil = remember { BatteryOptimizationUtil(context) }
    var showApiKey by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeepSpace, BgSpace)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Toolbar ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextSecondary)
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // ── AI Provider ───────────────────────────────────────────
                SettingsSection("AI Provider", Icons.Default.SmartToy) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(Constants.PROVIDER_OPENAI to "OpenAI", Constants.PROVIDER_GEMINI to "Gemini")
                            .forEach { (id, name) ->
                                val selected = uiState.aiProvider == id
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.setAiProvider(id) },
                                    label = { Text(name, color = if (selected) NovaCyan else TextSecondary) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NovaCyan.copy(0.15f),
                                        containerColor = BgCard
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        selectedBorderColor = NovaCyan,
                                        borderColor = BorderPrimary
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                    }

                    Spacer(Modifier.height(8.dp))

                    // API Key input
                    if (uiState.hasApiKey) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Success, modifier = Modifier.size(16.dp))
                            Text("API key is saved", style = MaterialTheme.typography.bodySmall, color = Success)
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { viewModel.clearApiKey() }) {
                                Text("Clear", color = Error, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    } else {
                        Text(
                            "No API key set. Add one to start chatting.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Warning
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.apiKey,
                        onValueChange = viewModel::onApiKeyChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("New API Key", color = TextTertiary) },
                        placeholder = { Text("sk-… or AI…", color = TextDisabled) },
                        visualTransformation = if (showApiKey) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null, tint = TextTertiary
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NovaCyan,
                            unfocusedBorderColor = BorderPrimary,
                            focusedContainerColor = BgCard,
                            unfocusedContainerColor = BgCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.saveApiKey() },
                        enabled = uiState.apiKey.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaCyan.copy(0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, null, tint = NovaCyan)
                        Spacer(Modifier.width(6.dp))
                        Text("Save API Key", color = NovaCyan)
                    }

                    uiState.savedMessage?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = Success)
                    }
                }

                // ── Voice ─────────────────────────────────────────────────
                SettingsSection("Voice", Icons.Default.Mic) {
                    SettingsSwitchRow(
                        title = "Wake Phrase Detection",
                        subtitle = "\"Hi NOVA\" always listens",
                        checked = uiState.wakePhraseEnabled,
                        onCheckedChange = viewModel::setWakePhraseEnabled
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Speech Speed: ${"%.1f".format(uiState.ttsSpeed)}×",
                        style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Slider(
                        value = uiState.ttsSpeed,
                        onValueChange = viewModel::setTtsSpeed,
                        valueRange = 0.5f..2.0f,
                        steps = 5,
                        colors = SliderDefaults.colors(thumbColor = NovaCyan, activeTrackColor = NovaCyan)
                    )
                    Text("Pitch: ${"%.1f".format(uiState.ttsPitch)}",
                        style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Slider(
                        value = uiState.ttsPitch,
                        onValueChange = viewModel::setTtsPitch,
                        valueRange = 0.5f..2.0f,
                        steps = 5,
                        colors = SliderDefaults.colors(thumbColor = NovaPurple, activeTrackColor = NovaPurple)
                    )
                }

                // ── Battery ───────────────────────────────────────────────
                SettingsSection("Battery", Icons.Default.BatteryChargingFull) {
                    val isExempt = batteryUtil.isIgnoringBatteryOptimizations()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isExempt) Icons.Default.CheckCircle else Icons.Default.Warning,
                            null,
                            tint = if (isExempt) Success else Warning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (isExempt) "Battery optimization disabled (optimal)" else "Battery optimization active",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isExempt) Success else Warning
                        )
                    }
                    if (!isExempt) {
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { batteryUtil.requestExemption(context) },
                            border = BorderStroke(1.dp, Warning),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Disable Optimization", color = Warning)
                        }
                    }
                }

                // ── Notifications ─────────────────────────────────────────
                SettingsSection("Notifications", Icons.Default.Notifications) {
                    SettingsSwitchRow(
                        title = "Enable Notifications",
                        subtitle = "Reminders, alarms, and system alerts",
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled
                    )
                }

                // ── About ─────────────────────────────────────────────────
                SettingsSection("About", Icons.Default.Info) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("NOVA", style = MaterialTheme.typography.bodyMedium, color = NovaCyan)
                        Text("v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                    Text("Neural Omniscient Virtual Assistant",
                        style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    Spacer(Modifier.height(4.dp))
                    Text("Built with Kotlin, Jetpack Compose, and ❤",
                        style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, BorderPrimary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(icon, null, tint = NovaCyan, modifier = Modifier.size(18.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            }
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NovaCyan.copy(0.7f),
                uncheckedTrackColor = BgSurfaceVar
            )
        )
    }
}
