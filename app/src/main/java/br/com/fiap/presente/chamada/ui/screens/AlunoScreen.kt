package br.com.fiap.presente.chamada.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.presente.chamada.domain.AlunoSituacao
import br.com.fiap.presente.chamada.domain.NivelAlerta
import br.com.fiap.presente.chamada.domain.RegraGatilho
import br.com.fiap.presente.chamada.domain.StatusCaso
import br.com.fiap.presente.chamada.ui.PresenteViewModel
import br.com.fiap.presente.chamada.ui.components.HistoricoAulas
import br.com.fiap.presente.chamada.ui.components.SeloNivel
import br.com.fiap.presente.chamada.ui.components.TelaPadrao
import br.com.fiap.presente.chamada.ui.components.corDoNivel
import br.com.fiap.presente.chamada.ui.components.formatarPercentual
import br.com.fiap.presente.chamada.ui.components.textoDoCaso
import br.com.fiap.presente.chamada.ui.theme.VerdeEscuro
import br.com.fiap.presente.chamada.ui.theme.VermelhoAlerta

/** Detalhe do aluno: frequência, motivo do alerta e abertura do caso de busca ativa. */
@Composable
fun AlunoScreen(
    alunoId: Long,
    viewModel: PresenteViewModel,
    snackbar: SnackbarHostState,
    onVoltar: () -> Unit,
) {
    val alunos by viewModel.alunos.collectAsStateWithLifecycle()
    val enviando by viewModel.enviando.collectAsStateWithLifecycle()
    val aluno = alunos.firstOrNull { it.id == alunoId }

    TelaPadrao(titulo = aluno?.nome ?: "Aluno", subtitulo = "Acompanhamento de frequência", snackbar = snackbar, onVoltar = onVoltar) { padding ->
        if (aluno == null) {
            Text("Carregando...", modifier = Modifier.padding(padding).padding(16.dp))
            return@TelaPadrao
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CardFrequencia(aluno)
            CardMotivo(aluno)
            CardFamilia(aluno)
            CardCaso(aluno, enviando, onAbrirCaso = { viewModel.abrirCaso(aluno.id) })
        }
    }
}

@Composable
private fun CardFrequencia(aluno: AlunoSituacao) {
    val (cor, _) = corDoNivel(aluno.nivel)
    CardBase {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Frequência", style = MaterialTheme.typography.bodySmall)
                Text(
                    formatarPercentual(aluno.resumo.percentual),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = cor,
                )
            }
            SeloNivel(aluno.nivel)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "${aluno.resumo.presencas} presenças · ${aluno.resumo.faltas} faltas em ${aluno.resumo.totalAulas} aulas · " +
                "${aluno.resumo.faltasSeguidas} falta(s) seguida(s)",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))
        Text("Últimas aulas", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(6.dp))
        HistoricoAulas(aluno.ultimasAulas, tamanho = 16)
    }
}

@Composable
private fun CardMotivo(aluno: AlunoSituacao) {
    val motivos = buildList {
        if (aluno.resumo.faltasSeguidas >= RegraGatilho.FALTAS_SEGUIDAS_ALERTA) {
            add("${aluno.resumo.faltasSeguidas} faltas seguidas (gatilho: ${RegraGatilho.FALTAS_SEGUIDAS_ALERTA})")
        } else if (aluno.resumo.faltasSeguidas >= RegraGatilho.FALTAS_SEGUIDAS_ATENCAO) {
            add("${aluno.resumo.faltasSeguidas} faltas seguidas")
        }
        if (aluno.resumo.percentual < RegraGatilho.FREQUENCIA_MINIMA) {
            add("Frequência abaixo de 80%: risco de perder o Pé-de-Meia")
        } else if (aluno.resumo.percentual < RegraGatilho.FREQUENCIA_ATENCAO) {
            add("Frequência chegando perto dos 80% do Pé-de-Meia")
        }
    }
    CardBase {
        Text("Por que este status?", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        if (motivos.isEmpty()) {
            Text("Frequência em dia. Nenhuma ação necessária.")
        } else {
            motivos.forEach { Text("• $it") }
        }
    }
}

@Composable
private fun CardFamilia(aluno: AlunoSituacao) {
    val idioma = when (aluno.idiomaFamilia) {
        "es" -> "Espanhol"
        "ht" -> "Crioulo haitiano"
        else -> "Português"
    }
    CardBase {
        Text("Família", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text("Responsável: ${aluno.responsavel}")
        Text("Idioma da conversa: $idioma")
        Text(
            "O contato é feito pelo assistente no WhatsApp, com consentimento do responsável (LGPD).",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun CardCaso(aluno: AlunoSituacao, enviando: Boolean, onAbrirCaso: () -> Unit) {
    CardBase {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val icone = when (aluno.statusCaso) {
                StatusCaso.ABERTO -> Icons.Filled.Verified
                StatusCaso.PENDENTE_ENVIO -> Icons.Filled.HourglassTop
                StatusCaso.SEM_CASO -> Icons.Filled.Forum
            }
            Icon(icone, null, tint = if (aluno.statusCaso == StatusCaso.SEM_CASO) VerdeEscuro else VermelhoAlerta)
            Spacer(Modifier.size(8.dp))
            Text("Busca ativa: ${textoDoCaso(aluno.statusCaso)}", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        when (aluno.statusCaso) {
            StatusCaso.SEM_CASO -> {
                if (aluno.nivel == NivelAlerta.ALERTA) {
                    Text(
                        "O aluno atingiu o gatilho. Ao abrir o caso, o assistente pergunta à família o motivo das faltas " +
                            "e o agente de busca ativa recebe o resumo com o encaminhamento sugerido.",
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onAbrirCaso,
                        enabled = !enviando,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VermelhoAlerta),
                    ) {
                        Text(if (enviando) "Abrindo caso..." else "Abrir caso de busca ativa")
                    }
                } else {
                    Text("O caso só é aberto quando o aluno atinge o gatilho (3 faltas seguidas ou frequência abaixo de 80%).")
                }
            }
            StatusCaso.PENDENTE_ENVIO -> Text(
                "Caso salvo no aparelho. Ele será enviado automaticamente na próxima sincronização com internet.",
            )
            StatusCaso.ABERTO -> Text(
                "Caso aberto no ms-casos. A família já está sendo contatada e o agente de busca ativa acompanha até o aluno voltar.",
            )
        }
    }
}

@Composable
private fun CardBase(conteudo: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) { conteudo() }
    }
}
