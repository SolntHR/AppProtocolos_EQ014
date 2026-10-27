package com.example.appprotocolos_eq014.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaInduztek = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = Blanco,
    primaryContainer = AzulSuave,
    onPrimaryContainer = AzulPrimario,
    secondary = NaranjaAcento,
    onSecondary = Blanco,
    secondaryContainer = NaranjaSuave,
    onSecondaryContainer = AmarilloAviso,
    background = Superficie,
    onBackground = TextoPrincipal,
    surface = Blanco,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieFuerte,
    onSurfaceVariant = TextoSecundario,
    outline = Borde,
    outlineVariant = SuperficieFuerte,
    error = RojoError,
    onError = Blanco,
    errorContainer = RojoSuave,
    onErrorContainer = RojoError
)

@Composable
fun AppProtocolos_EQ014Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaInduztek,
        typography = Typography,
        content = content
    )
}
