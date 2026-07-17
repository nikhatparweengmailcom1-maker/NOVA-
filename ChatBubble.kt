package com.nova.assistant.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.nova.assistant.domain.model.Message
import com.nova.assistant.domain.model.MessageRole
import com.nova.assistant.presentation.ui.theme.*

/**
 * A single chat message bubble.
 */
@Composable
fun ChatBubble(
    message: Message,
    onSpeak: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER
    val clipboard = LocalClipboardManager.current
    var showActions by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            // NOVA avatar dot
            if (!isUser) {
                NovaDot(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(28.dp)
                )
            }

            Column(
                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                // Bubble
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = if (isUser) 20.dp else 4.dp,
                                bottomEnd   = if (isUser) 4.dp  else 20.dp
                            )
                        )
                        .background(
                            brush = if (isUser) {
                                Brush.linearGradient(ChatGradientUser)
                            } else {
                                Brush.linearGradient(ChatGradientNova)
                            }
                        )
                        .border(
                            width = 0.5.dp,
                            brush = Brush.linearGradient(
                                if (isUser) listOf(NovaCyan.copy(alpha = 0.4f), NovaPurple.copy(alpha = 0.2f))
                                else        listOf(BorderPrimary, Divider)
                            ),
                            shape = RoundedCornerShape(
                                topStart = 20.dp, topEnd = 20.dp,
                                bottomStart = if (isUser) 20.dp else 4.dp,
                                bottomEnd   = if (isUser) 4.dp  else 20.dp
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (message.isError) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Error,
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isUser) TextPrimary else TextSecondary
                        )
                    }
                }

                // Action row
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timestamp
                    Text(
                        text = message.timestamp.toFormattedTime(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDisabled
                    )
                    if (!isUser) {
                        // Copy button
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(message.content))
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = TextDisabled,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        // Speak button
                        IconButton(
                            onClick = { onSpeak(message.content) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Speak",
                                tint = TextDisabled,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            // User avatar dot
            if (isUser) {
                UserDot(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun NovaDot(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.radialGradient(listOf(NovaCyan.copy(0.8f), NovaPurple.copy(0.6f), BgCard))
            )
            .border(0.5.dp, NovaCyan.copy(0.5f), RoundedCornerShape(50))
    ) {
        Text("N", style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

@Composable
private fun UserDot(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(BgElevated)
            .border(0.5.dp, BorderPrimary, RoundedCornerShape(50))
    ) {
        Text("U", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

private fun Long.toFormattedTime(): String {
    val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}
