package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ForensicDarkColorScheme = darkColorScheme(
    primary = ForensicCyanPrimary,
    onPrimary = ForensicCyanOnPrimary,
    primaryContainer = ForensicCyanContainer,
    onPrimaryContainer = ForensicCyanOnContainer,

    secondary = CompliantGreen,
    onSecondary = CompliantGreenOn,
    secondaryContainer = CompliantGreenContainer,
    onSecondaryContainer = CompliantGreenOnContainer,

    tertiary = WarningAmber,
    onTertiary = WarningAmberOn,
    tertiaryContainer = WarningAmberContainer,
    onTertiaryContainer = WarningAmberOnContainer,

    background = DarkBackground,
    onBackground = TextPrimaryDark,

    surface = DarkSurface,
    onSurface = TextPrimaryDark,

    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,

    surfaceContainerHighest = DarkSurfaceHighlight,

    outline = HighContrastBorder,
    outlineVariant = HighContrastBorder,

    error = RejectedRed,
    onError = RejectedRedOn,
    errorContainer = RejectedRedContainer,
    onErrorContainer = RejectedRedOnContainer
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ForensicDarkColorScheme,
        typography = Typography,
        content = content
    )
}
