package com.repforge.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

/**
 * RepForge dark color scheme.
 * The app is designed as a high-contrast dark-only theme for gym environments.
 */
private val RepForgeDarkColorScheme = darkColorScheme(
    primary = ForgeAmber,
    onPrimary = CarbonSlate,
    primaryContainer = ForgeAmberDark,
    onPrimaryContainer = TextPrimary,

    secondary = KineticLime,
    onSecondary = CarbonSlate,
    secondaryContainer = KineticLimeDark,
    onSecondaryContainer = TextPrimary,

    tertiary = WarmupColor,
    onTertiary = CarbonSlate,

    background = CarbonSlate,
    onBackground = TextPrimary,

    surface = CarbonSlateLight,
    onSurface = TextPrimary,
    surfaceVariant = CarbonSlateSurface,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = CarbonSlate,
    errorContainer = DeclineRed,
    onErrorContainer = TextPrimary,

    outline = TextTertiary,
    outlineVariant = CarbonSlateCard
)

/**
 * RepForge Material 3 theme.
 * Always dark — designed for in-gym use with high-contrast colors.
 */
@Composable
fun RepForgeTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = RepForgeDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.statusBarColor = CarbonSlate.toArgb()
            window?.navigationBarColor = CarbonSlate.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RepForgeTypography,
        content = content
    )
}
