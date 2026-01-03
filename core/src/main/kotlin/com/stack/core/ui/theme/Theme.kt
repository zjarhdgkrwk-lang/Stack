package com.stack.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Persimmon,
    onPrimary = RicePaper,
    primaryContainer = Persimmon.copy(alpha = 0.12f),
    onPrimaryContainer = Persimmon,
    secondary = FadedDenim,
    onSecondary = RicePaper,
    secondaryContainer = FadedDenim.copy(alpha = 0.12f),
    onSecondaryContainer = FadedDenim,
    tertiary = FadedDenim,
    onTertiary = RicePaper,
    background = RicePaper,
    onBackground = SootBlack,
    surface = RicePaper,
    onSurface = SootBlack,
    surfaceVariant = CloudyBeige,
    onSurfaceVariant = SootBlack.copy(alpha = 0.7f),
    outline = SootBlack.copy(alpha = 0.3f),
    outlineVariant = SootBlack.copy(alpha = 0.1f),
    error = ErrorRed,
    onError = RicePaper
)

private val DarkColorScheme = darkColorScheme(
    primary = LuminousPersimmon,
    onPrimary = WarmCharcoal,
    primaryContainer = LuminousPersimmon.copy(alpha = 0.12f),
    onPrimaryContainer = LuminousPersimmon,
    secondary = AshBlue,
    onSecondary = WarmCharcoal,
    secondaryContainer = AshBlue.copy(alpha = 0.12f),
    onSecondaryContainer = AshBlue,
    tertiary = AshBlue,
    onTertiary = WarmCharcoal,
    background = WarmCharcoal,
    onBackground = OldPaper,
    surface = WarmCharcoal,
    onSurface = OldPaper,
    surfaceVariant = DarkEspresso,
    onSurfaceVariant = OldPaper.copy(alpha = 0.7f),
    outline = OldPaper.copy(alpha = 0.3f),
    outlineVariant = OldPaper.copy(alpha = 0.1f),
    error = ErrorRed,
    onError = WarmCharcoal
)

@Composable
fun StackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StackTypography,
        shapes = StackShapes,
        content = content
    )
}
