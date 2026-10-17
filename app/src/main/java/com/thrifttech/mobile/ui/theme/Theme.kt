package com.thrifttech.mobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ThriftColorScheme = darkColorScheme(
    primary = ThriftTeal,
    onPrimary = ThriftNavy,
    primaryContainer = ThriftTealDark,
    onPrimaryContainer = ThriftWhite,

    secondary = ThriftLightNavy,
    onSecondary = ThriftWhite,

    background = ThriftNavy,
    onBackground = ThriftWhite,

    surface = ThriftLightNavy,
    onSurface = ThriftWhite,
    surfaceVariant = ThriftLightNavy,
    onSurfaceVariant = ThriftLightGray,

    error = ThriftError,
    onError = ThriftWhite,

    outline = ThriftTeal
)

@Composable
fun ThriftTechTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = ThriftColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ThriftNavy.toArgb()
            window.navigationBarColor = ThriftNavy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}