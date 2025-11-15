package com.tesis.appmovil.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
private val DarkColorScheme = darkColorScheme(
    primary = Purple400,            // Morado claro para modo oscuro
    onPrimary = Purple900,
    primaryContainer = Purple800,
    onPrimaryContainer = Purple100,

    secondary = Blue500,            // Azul medio
    onSecondary = Blue900,
    secondaryContainer = Blue800,
    onSecondaryContainer = Blue100,

    tertiary = Yellow800,           // Amarillo de acento

    background = Grey900,
    onBackground = Grey100,

    surface = Grey800,
    onSurface = Grey200,

    error = Red400,
    onError = Color.White
)
private val LightColorScheme = lightColorScheme(
    primary = Purple700,            // Morado oscuro para modo claro
    onPrimary = Color.White,
    primaryContainer = Purple100,
    onPrimaryContainer = Purple900,

    secondary = Blue700,
    onSecondary = Color.White,
    secondaryContainer = Blue100,
    onSecondaryContainer = Blue900,

    tertiary = Yellow900,

    background = Grey50,
    onBackground = Grey900,

    surface = Grey100,
    onSurface = Grey900,

    error = Red600,
    onError = Color.White
)

@Composable
fun AppMovilTesisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}