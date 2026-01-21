package com.mobelio.bill.split.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BlobPink,
    onPrimary = Color.White,
    primaryContainer = BlobPurple.copy(alpha = 0.3f),
    onPrimaryContainer = Color.White,
    secondary = BlobCyan,
    onSecondary = Color.Black,
    secondaryContainer = BlobCyan.copy(alpha = 0.3f),
    onSecondaryContainer = Color.White,
    tertiary = BlobYellow,
    onTertiary = Color.Black,
    tertiaryContainer = BlobYellow.copy(alpha = 0.3f),
    onTertiaryContainer = Color.White,
    background = DarkBackground,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = CardGlass,
    onSurfaceVariant = TextGray,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BlobPink,
    onPrimary = Color.White,
    primaryContainer = BlobPink.copy(alpha = 0.15f),
    onPrimaryContainer = BlobPink,
    secondary = BlobCyan,
    onSecondary = Color.Black,
    secondaryContainer = BlobCyan.copy(alpha = 0.15f),
    onSecondaryContainer = BlobCyan,
    tertiary = BlobYellow,
    onTertiary = Color.Black,
    tertiaryContainer = BlobYellow.copy(alpha = 0.15f),
    onTertiaryContainer = BlobYellow,
    background = DarkBackground,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = CardGlass,
    onSurfaceVariant = TextGray,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun BIllSplitTheme(
    darkTheme: Boolean = true, // Always use dark theme for this design
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // Always dark

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}