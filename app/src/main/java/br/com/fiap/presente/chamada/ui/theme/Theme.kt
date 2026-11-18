package br.com.fiap.presente.chamada.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CoresPresente = lightColorScheme(
    primary = VerdePresente,
    onPrimary = Color.White,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = VerdeEscuro,
    secondary = AmbarAtencao,
    secondaryContainer = VerdeClaro,
    onSecondaryContainer = VerdeEscuro,
    surfaceVariant = Color(0xFFE3EAE7),
    surfaceContainerHighest = Color(0xFFE3EAE7),
    error = VermelhoAlerta,
    errorContainer = VermelhoClaro,
    background = FundoClaro,
    surface = Color.White,
    onBackground = TextoEscuro,
    onSurface = TextoEscuro,
)

@Composable
fun PresenteTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CoresPresente, content = content)
}
