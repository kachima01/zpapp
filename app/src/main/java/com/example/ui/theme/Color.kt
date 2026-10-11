package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// High-contrast tactical dark theme palette for digital forensics
val DarkBackground = Color(0xFF090D14)
val DarkSurface = Color(0xFF101726)
val DarkSurfaceElevated = Color(0xFF172033)
val DarkSurfaceHighlight = Color(0xFF1E2B45)

val HighContrastBorder = Color(0xFF2A3B57)
val HighContrastBorderFocused = Color(0xFF38BDF8)

// Accent Colors (Strictly High-Contrast, WCAG AAA compliant against dark surfaces)
val ForensicCyanPrimary = Color(0xFF38BDF8) // Primary active & selection
val ForensicCyanOnPrimary = Color(0xFF001E2E)
val ForensicCyanContainer = Color(0xFF034A70)
val ForensicCyanOnContainer = Color(0xFFD3F2FF)

val CompliantGreen = Color(0xFF22C55E) // Lawful authority, successful match, available tool
val CompliantGreenOn = Color(0xFF00220A)
val CompliantGreenContainer = Color(0xFF05491D)
val CompliantGreenOnContainer = Color(0xFFA7F3D0)

val WarningAmber = Color(0xFFFBBF24) // Restriced scope, intermediate caution
val WarningAmberOn = Color(0xFF2B1800)
val WarningAmberContainer = Color(0xFF5A3900)
val WarningAmberOnContainer = Color(0xFFFDE68A)

val RejectedRed = Color(0xFFF87171) // Excluded / rejected methods, legal barrier
val RejectedRedOn = Color(0xFF2E0404)
val RejectedRedContainer = Color(0xFF6B1717)
val RejectedRedOnContainer = Color(0xFFFECACA)

val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextTertiaryDark = Color(0xFF64748B)
