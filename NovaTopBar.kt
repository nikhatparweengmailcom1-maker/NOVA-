package com.nova.assistant.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.nova.assistant.domain.model.VoiceState
import com.nova.assistant.presentation.ui.theme.*

@Composable
fun NovaTopBar(
    voiceState: VoiceState,
    onSettingsClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when (voiceState) {
            is VoiceState.Listening  -> NovaCyan
            is VoiceState.Processing -> NovaPurple
            is VoiceState.Speaking   -> NovaOrange
            is VoiceState.Paused     -> TextTertiary
            is VoiceState.Error      -> Error
            else                     -> NovaCyan.copy(alpha = 0.5f)
        },
        animationSpec = tween(300),
        label = "statusColor"
    )

    val statusText = when (voiceState) {
        is VoiceState.Idle       -> "Ready"
        is VoiceState.Listening  -> "Listening…"
        is VoiceState.Processing -> "Thinking…"
        is VoiceState.Speaking   -> "Speaking…"
        is VoiceState.Paused     -> "Paused"
        is VoiceState.Error      -> "Error"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(BgDeepSpace, BgDeepSpace.copy(alpha = 0.95f)))
            )
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        // NOVA Logo / Status
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Text(
                text = "NOVA",
                style = MaterialTheme.typography.titleLarge,
                color = NovaCyan
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Status indicator dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(statusColor)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }
        }

        // Actions
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onClearClick) {
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = "Clear conversation",
                    tint = TextTertiary
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary
                )
            }
        }
    }
}
