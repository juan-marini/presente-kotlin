package br.com.fiap.presente.chamada.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.presente.chamada.domain.NivelAlerta
import br.com.fiap.presente.chamada.domain.StatusCaso
import br.com.fiap.presente.chamada.ui.theme.AmbarAtencao
import br.com.fiap.presente.chamada.ui.theme.AmbarClaro
import br.com.fiap.presente.chamada.ui.theme.VerdeClaro
import br.com.fiap.presente.chamada.ui.theme.VerdeEscuro
import br.com.fiap.presente.chamada.ui.theme.VermelhoAlerta
import br.com.fiap.presente.chamada.ui.theme.VermelhoClaro

/** Estrutura padrão das telas internas: barra superior com voltar + snackbar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPadrao(
    titulo: String,
    snackbar: SnackbarHostState,
    onVoltar: (() -> Unit)?,
    subtitulo: String? = null,
    bottomBar: @Composable () -> Unit = {},
    conteudo: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Column {
                        Text(titulo, fontWeight = FontWeight.Bold)
                        if (subtitulo != null) {
                            Text(subtitulo, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                navigationIcon = {
                    if (onVoltar != null) {
                        IconButton(onClick = onVoltar) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background,
        content = conteudo,
    )
}

fun corDoNivel(nivel: NivelAlerta): Pair<Color, Color> = when (nivel) {
    NivelAlerta.OK -> VerdeEscuro to VerdeClaro
    NivelAlerta.ATENCAO -> AmbarAtencao to AmbarClaro
    NivelAlerta.ALERTA -> VermelhoAlerta to VermelhoClaro
}

fun textoDoNivel(nivel: NivelAlerta): String = when (nivel) {
    NivelAlerta.OK -> "Em dia"
    NivelAlerta.ATENCAO -> "Atenção"
    NivelAlerta.ALERTA -> "Alerta"
}

fun textoDoCaso(status: StatusCaso): String = when (status) {
    StatusCaso.SEM_CASO -> "Sem caso"
    StatusCaso.PENDENTE_ENVIO -> "Caso pendente de envio"
    StatusCaso.ABERTO -> "Caso aberto"
}

/** Etiqueta colorida (selo) com texto curto. */
@Composable
fun Selo(texto: String, corTexto: Color, corFundo: Color, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        color = corTexto,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(corFundo, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

@Composable
fun SeloNivel(nivel: NivelAlerta) {
    val (texto, fundo) = corDoNivel(nivel)
    Selo(textoDoNivel(nivel), texto, fundo)
}

/** Bolinhas das últimas aulas: verde = presente, vermelho = falta. */
@Composable
fun HistoricoAulas(aulas: List<Boolean>, tamanho: Int = 12) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        aulas.forEach { presente ->
            Box(
                Modifier
                    .size(tamanho.dp)
                    .background(if (presente) VerdeEscuro else VermelhoAlerta, CircleShape)
            )
        }
    }
}

fun formatarPercentual(valor: Double): String = "${(valor * 100).toInt()}%"
