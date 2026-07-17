package com.nova.assistant.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.nova.assistant.presentation.ui.theme.NovaCyan
import com.nova.assistant.presentation.ui.theme.NovaPurple
import kotlin.math.sin

/**
 * Animated voice waveform bars shown during listening/speaking states.
 */
@Composable
fun VoiceWaveform(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 20,
    color: Color = NovaCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing)
        ),
        label = "phase"
    )

    val amplitudeAnim by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.15f,
        animationSpec = tween(300),
        label = "amplitude"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(48.dp)) {
        val barWidth = size.width / (barCount * 2f)
        val maxHeight = size.height * 0.9f
        val minHeight = size.height * 0.1f

        for (i in 0 until barCount) {
            val x = i * size.width / barCount + barWidth / 2f
            val waveVal = sin(phase + i * 0.5f).toFloat()
            val barHeight = (minHeight + (waveVal + 1f) / 2f * (maxHeight - minHeight)) * amplitudeAnim

            val alpha = 0.5f + (waveVal + 1f) / 4f
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NovaCyan.copy(alpha = alpha),
                        NovaPurple.copy(alpha = alpha * 0.6f)
                    )
                ),
                start = Offset(x, size.height / 2f - barHeight / 2f),
                end   = Offset(x, size.height / 2f + barHeight / 2f),
                strokeWidth = barWidth * 0.7f,
                cap = StrokeCap.Round
            )
        }
    }
}
