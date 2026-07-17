package com.nova.assistant.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nova.assistant.domain.model.VoiceState
import com.nova.assistant.presentation.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * NOVA holographic glowing AI orb.
 * Animates differently based on VoiceState.
 */
@Composable
fun NovaOrb(
    voiceState: VoiceState,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")

    // Rotation animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (voiceState) {
                    is VoiceState.Listening  -> 2000
                    is VoiceState.Processing -> 1200
                    is VoiceState.Speaking   -> 1800
                    else                     -> 5000
                },
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )

    // Pulse scale animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (voiceState) {
                    is VoiceState.Listening  -> 600
                    is VoiceState.Speaking   -> 400
                    else                     -> 1400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Glow intensity
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Ring orbit angle
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "orbit"
    )

    val coreColor = when (voiceState) {
        is VoiceState.Listening  -> NovaCyan
        is VoiceState.Processing -> NovaPurple
        is VoiceState.Speaking   -> NovaOrange.copy(alpha = 0.9f)
        is VoiceState.Paused     -> TextTertiary
        is VoiceState.Error      -> Error
        else                     -> NovaCyan
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2f

            scale(pulseScale, pivot = center) {
                // ── Outer glow halo ──────────────────────────────────────
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            coreColor.copy(alpha = glowAlpha * 0.35f),
                            coreColor.copy(alpha = 0.0f)
                        ),
                        center = center,
                        radius = radius * 1.4f
                    ),
                    radius = radius * 1.4f,
                    center = center
                )

                // ── Mid ring ──────────────────────────────────────────────
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NovaPurple.copy(alpha = glowAlpha * 0.5f),
                            NovaCyan.copy(alpha = glowAlpha * 0.2f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )

                // ── Core sphere ────────────────────────────────────────────
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            coreColor.copy(alpha = 0.95f),
                            NovaPurple.copy(alpha = 0.6f),
                            BgDeepSpace.copy(alpha = 0.9f)
                        ),
                        center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f),
                        radius = radius * 0.7f
                    ),
                    radius = radius * 0.6f,
                    center = center
                )

                // ── Rotating ring ─────────────────────────────────────────
                rotate(degrees = rotation, pivot = center) {
                    drawOrbitRing(center, radius * 0.85f, coreColor.copy(alpha = 0.6f))
                }

                // ── Counter-rotating inner ring ───────────────────────────
                rotate(degrees = -rotation * 1.5f, pivot = center) {
                    drawOrbitRing(center, radius * 0.65f, NovaPurple.copy(alpha = 0.5f), dotCount = 4)
                }

                // ── Orbiting dot ───────────────────────────────────────────
                val orbitRadiusDot = radius * 0.85f
                val dotAngleRad = Math.toRadians(orbitAngle.toDouble())
                val dotX = center.x + orbitRadiusDot * cos(dotAngleRad).toFloat()
                val dotY = center.y + orbitRadiusDot * sin(dotAngleRad).toFloat()
                drawCircle(
                    color = coreColor,
                    radius = radius * 0.06f,
                    center = Offset(dotX, dotY)
                )
                // Dot glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(coreColor.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(dotX, dotY),
                        radius = radius * 0.12f
                    ),
                    radius = radius * 0.12f,
                    center = Offset(dotX, dotY)
                )
            }
        }
    }
}

private fun DrawScope.drawOrbitRing(
    center: Offset,
    radius: Float,
    color: Color,
    dotCount: Int = 6
) {
    val dotRadius = radius * 0.025f
    for (i in 0 until dotCount) {
        val angle = Math.toRadians((i * 360.0 / dotCount))
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        drawCircle(color = color, radius = dotRadius, center = Offset(x, y))
    }
    // Draw thin ring line
    drawCircle(
        color = color.copy(alpha = 0.3f),
        radius = radius,
        center = center,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
    )
}
