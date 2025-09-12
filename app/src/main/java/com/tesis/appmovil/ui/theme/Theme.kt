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
//    primary = Blue300,            // Color principal para el modo oscuro (más claro)
//    onPrimary = Blue900,          // Texto/iconos sobre el color primario
//    primaryContainer = Blue800,   // Contenedor relacionado al primario
//    onPrimaryContainer = Blue100, // Texto/iconos sobre el primaryContainer

    primary = Purple300,            // Color principal para el modo oscuro (más claro)
    onPrimary = Purple900,          // Texto/iconos sobre el color primario
    primaryContainer = Purple800,   // Contenedor relacionado al primario
    onPrimaryContainer = Purple100,

    secondary = Blue700,          // Color secundario o de acento
    onSecondary = Blue900,        // Texto/iconos sobre el secundario
    secondaryContainer = Blue800,
    onSecondaryContainer = Blue100,

    tertiary = Yellow300,           // Color terciario o de acento extra

    background = Grey900,           // Color de fondo de la app
    onBackground = Grey100,         // Color del texto sobre el fondo

    surface = Grey800,              // Color de superficies (cards, menús)
    onSurface = Grey200,            // Color del texto sobre superficies

    error = Red400,                 // Color para errores
    onError = Color.White           // Texto sobre color de error
)

private val LightColorScheme = lightColorScheme(
//    primary = Blue500,              // Color principal para el modo claro (más oscuro)
//    onPrimary = Color.White,        // Texto/iconos sobre el color primario
//    primaryContainer = Blue100,
//    onPrimaryContainer = Blue900,
    primary = Purple700,              // Color principal para el modo claro (más oscuro)
    onPrimary = Color.White,        // Texto/iconos sobre el color primario
    primaryContainer = Purple100,
    onPrimaryContainer = Purple900,

    secondary = Blue500,
    onSecondary = Color.White,
    secondaryContainer = Blue100,
    onSecondaryContainer = Blue900,

    tertiary = Yellow500,

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