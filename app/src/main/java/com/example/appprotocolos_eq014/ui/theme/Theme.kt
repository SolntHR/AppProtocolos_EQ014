package com.example.appprotocolos_eq014.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AzulOscuro,
    secondary = NaranjaAcento,
    background = FondoOscuro,
    surface = SuperficieOscura,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextoPrincipalOscuro,
    onSurface = TextoSecundarioOscuro,
    outline = BordeOscuro,
    error = ErrorRojo
)

private val LightColorScheme = lightColorScheme(
    primary = AzulPrimario,
    secondary = NaranjaAcento,
    background = FondoClaro,
    surface = SuperficieClara,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextoPrincipalClaro,
    onSurface = TextoSecundarioClaro,
    outline = BordeClaro,
    error = ErrorRojo
)

@Composable
fun AppProtocolos_EQ014Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // barrita android va con la app
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}