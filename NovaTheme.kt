package com.nova.assistant.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * NOVA dark holographic color scheme wired to Material3.
 * Edge-to-edge is handled by MainActivity.enableEdgeToEdge() — no
 * additional system-bar tinting is needed here.
 */
private val NovaDarkColorScheme = darkColorScheme(
    // Primary brand: cyan
    primary          = NovaCyan,
    onPrimary        = BgDeepSpace,
    primaryContainer = NovaCyanDim,
    onPrimaryContainer = TextPrimary,

    // Secondary: purple
    secondary          = NovaPurple,
    onSecondary        = BgDeepSpace,
    secondaryContainer = NovaPurpleDim,
    onSecondaryContainer = TextPrimary,

    // Tertiary: orange accent
    tertiary          = NovaOrange,
    onTertiary        = BgDeepSpace,
    tertiaryContainer = NovaOrangeDim,
    onTertiaryContainer = TextPrimary,

    // Surfaces
    background    = BgDeepSpace,
    onBackground  = TextPrimary,
    surface       = BgSpace,
    onSurface     = TextPrimary,
    surfaceVariant    = BgSurfaceVar,
    onSurfaceVariant  = TextSecondary,

    // Outline
    outline       = BorderPrimary,
    outlineVariant = Divider,

    // Status
    error    = Error,
    onError  = BgDeepSpace,
    errorContainer    = Error,
    onErrorContainer  = BgDeepSpace,

    // Inverse
    inverseSurface       = TextPrimary,
    inverseOnSurface     = BgDeepSpace,
    inversePrimary       = BgDeepSpace,
    scrim                = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)
)

@Composable
fun NovaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NovaDarkColorScheme,
        typography  = NovaTypography,
        content     = content
    )
}
