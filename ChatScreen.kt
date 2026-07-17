package com.nova.assistant.presentation.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nova.assistant.domain.model.VoiceState
import com.nova.assistant.presentation.ui.components.*
import com.nova.assistant.presentation.ui.theme.*
import com.nova.assistant.presentation.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToTodos: () -> Unit,
    onNavigateToQRScanner: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startVoiceInput()
    }

    // Auto-scroll to latest message
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BgDeepSpace, BgSpace, BgDeepSpace)
                )
            )
    ) {
        // Grid background overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 60f
            val gridColor = BorderPrimary.copy(alpha = 0.15f)
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), 0.5f)
                x += gridSpacing
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 0.5f)
                y += gridSpacing
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────
            NovaTopBar(
                voiceState = voiceState,
                onSettingsClick = onNavigateToSettings,
                onClearClick = { viewModel.clearConversation() }
            )

            // ── NOVA orb + waveform ───────────────────────────────────────
            AnimatedVisibility(
                visible = uiState.messages.isEmpty() ||
                        voiceState is VoiceState.Listening ||
                        voiceState is VoiceState.Speaking,
                enter = fadeIn() + expandVertically(),
                exit  = fadeOut() + shrinkVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    NovaOrb(
                        voiceState = voiceState,
                        size = 160.dp
                    )
                    Spacer(Modifier.height(8.dp))
                    VoiceWaveform(
                        isActive = voiceState is VoiceState.Listening || voiceState is VoiceState.Speaking,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when (voiceState) {
                            VoiceState.Idle       -> "Say \"Hi NOVA\" to wake me"
                            VoiceState.Listening  -> "Listening…"
                            VoiceState.Processing -> "Thinking…"
                            is VoiceState.Speaking -> "Speaking…"
                            VoiceState.Paused     -> "Paused — say \"Resume\" to continue"
                            is VoiceState.Error   -> (voiceState as VoiceState.Error).message
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when (voiceState) {
                            is VoiceState.Listening  -> NovaCyan
                            is VoiceState.Processing -> NovaPurple
                            is VoiceState.Error      -> Error
                            else -> TextTertiary
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            // ── Quick command chips ───────────────────────────────────────
            if (uiState.messages.isEmpty()) {
                QuickCommandChips(
                    onCommandSelected = { cmd ->
                        viewModel.onInputChanged(cmd)
                    }
                )
                // Feature nav chips
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallNavChip("Reminders", Icons.Default.Alarm, onNavigateToReminders)
                    SmallNavChip("Todos", Icons.Default.CheckCircle, onNavigateToTodos)
                    SmallNavChip("Scan QR", Icons.Default.QrCodeScanner, onNavigateToQRScanner)
                }
            }

            // ── Messages list ─────────────────────────────────────────────
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatBubble(
                        message = message,
                        onSpeak = { text -> viewModel.speakText(text) }
                    )
                }
                if (uiState.isLoading) {
                    item { ThinkingIndicator() }
                }
            }

            // ── Input bar ─────────────────────────────────────────────────
            InputBar(
                text = uiState.inputText,
                onTextChange = viewModel::onInputChanged,
                onSend = {
                    keyboard?.hide()
                    viewModel.sendTextMessage()
                },
                onVoice = {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                isListening = voiceState is VoiceState.Listening,
                isLoading = uiState.isLoading
            )
        }

        // ── Error snackbar ─────────────────────────────────────────────────
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.dismissError() }) {
                        Text("Dismiss", color = NovaCyan)
                    }
                },
                containerColor = BgCard,
                contentColor = Error
            ) {
                Text(error)
            }
        }
    }
}

@Composable
private fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoice: () -> Unit,
    isListening: Boolean,
    isLoading: Boolean
) {
    val borderAlpha by animateFloatAsState(
        if (isListening) 1f else 0.4f,
        animationSpec = tween(300), label = "border"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("Message NOVA…", color = TextDisabled, style = MaterialTheme.typography.bodyMedium)
            },
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NovaCyan,
                unfocusedBorderColor = BorderPrimary,
                focusedContainerColor = BgCard,
                unfocusedContainerColor = BgCard,
                cursorColor = NovaCyan,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            maxLines = 4,
            singleLine = false
        )

        // Mic / Stop button
        val micPulse by animateFloatAsState(
            if (isListening) 1.15f else 1f,
            animationSpec = if (isListening)
                infiniteRepeatable(tween(400), RepeatMode.Reverse)
            else tween(200),
            label = "mic_pulse"
        )
        FilledIconButton(
            onClick = onVoice,
            modifier = Modifier.size(52.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isListening) Error else BgCard
            ),
            shape = CircleShape
        ) {
            Icon(
                if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop" else "Voice",
                tint = if (isListening) Color.White else NovaCyan
            )
        }

        // Send button
        AnimatedVisibility(visible = text.isNotBlank() && !isLoading) {
            FilledIconButton(
                onClick = onSend,
                modifier = Modifier.size(52.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = NovaCyan.copy(alpha = 0.15f)
                ),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = NovaCyan)
            }
        }
    }
}

@Composable
private fun ThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    val offset by infiniteTransition.animateFloat(
        0f, 6f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "dot"
    )
    Row(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { i ->
            val dotOffset by rememberInfiniteTransition(label = "dot$i").animateFloat(
                0f, 6f,
                animationSpec = infiniteRepeatable(
                    tween(600, delayMillis = i * 150),
                    RepeatMode.Reverse
                ),
                label = "dot_anim$i"
            )
            Box(
                modifier = Modifier
                    .offset(y = (-dotOffset).dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(NovaCyan.copy(alpha = 0.7f))
            )
        }
        Spacer(Modifier.width(6.dp))
        Text("NOVA is thinking…", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
    }
}

@Composable
private fun SmallNavChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    SuggestionChip(
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = NovaCyan)
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        },
        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = BgCard),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true, borderColor = BorderPrimary, borderWidth = 0.5.dp
        ),
        shape = RoundedCornerShape(20.dp)
    )
}
