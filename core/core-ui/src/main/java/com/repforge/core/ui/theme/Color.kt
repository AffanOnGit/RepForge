package com.repforge.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * RepForge color palette.
 * High-contrast dark theme designed for in-gym visibility.
 */

// Primary background — Carbon Slate
val CarbonSlate = Color(0xFF0B0D10)
val CarbonSlateLight = Color(0xFF141820)
val CarbonSlateSurface = Color(0xFF1A1F2A)
val CarbonSlateCard = Color(0xFF212733)

// Primary accent — Forge Amber (action, CTAs, active states)
val ForgeAmber = Color(0xFFFF6600)
val ForgeAmberDark = Color(0xFFCC5200)
val ForgeAmberLight = Color(0xFFFF8533)
val ForgeAmberSubtle = Color(0x33FF6600)

// Secondary accent — Kinetic Lime (progress, success, completion)
val KineticLime = Color(0xFFD4FF00)
val KineticLimeDark = Color(0xFFAACC00)
val KineticLimeSubtle = Color(0x33D4FF00)

// Progression status colors (accessible — not relying on color alone)
val ProgressGreen = Color(0xFF4CAF50)
val PlateauYellow = Color(0xFFFFC107)
val DeclineRed = Color(0xFFF44336)

// Text colors
val TextPrimary = Color(0xFFF0F0F0)
val TextSecondary = Color(0xFFB0B8C8)
val TextTertiary = Color(0xFF6B7588)
val TextGhost = Color(0xFF4A5568) // For ghost text (previous session values)

// Functional colors
val ErrorRed = Color(0xFFCF6679)
val SuccessGreen = Color(0xFF81C784)
val WarningAmber = Color(0xFFFFB74D)

// Set type colors
val WarmupColor = Color(0xFF64B5F6)  // Light blue
val WorkingColor = ForgeAmber
val DropSetColor = Color(0xFFBA68C8) // Purple
val FailureColor = DeclineRed
