package br.com.fiap.presente.chamada.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.presente.chamada.domain.NivelAlerta
import br.com.fiap.presente.chamada.ui.PresenteViewModel
import br.com.fiap.presente.chamada.ui.components.Selo
import br.com.fiap.presente.chamada.ui.components.TelaPadrao
import br.com.fiap.presente.chamada.ui.theme.AmbarAtencao
import br.com.fiap.presente.chamada.ui.theme.AmbarClaro
import br.com.fiap.presente.chamada.ui.theme.VermelhoAlerta
import br.com.fiap.presente.chamada.ui.theme.VermelhoClaro

/** Lista das turmas do professor, com progresso da chamada de hoje e alunos em risco. */
@Composable
fun TurmasScreen(
    viewModel: PresenteViewModel,
    snackbar: SnackbarHostState,
    onVoltar: () -> Unit,
    onAbrirTurma: (Long) -> Unit,
) {
    val turmas by viewModel.turmas.collectAsStateWithLifecycle()
    val alunos by viewModel.alunos.collectAsStateWithLifecycle()
    val pendentes by viewModel.pendentes.collectAsStateWithLifecycle()
    val enviando by viewModel.enviando.collectAsStateWithLifecycle()

    TelaPadrao(titulo = "Minhas turmas", subtitulo = "Escolha a turma para fazer a chamada", snackbar = snackbar, onVoltar = onVoltar) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudSync, null)
                        Text(
                            if (pendentes == 0) "Tudo sincronizado" else "$pendentes registro(s) aguardando envio",
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        )
                        TextButton(onClick = viewModel::sincronizar, enabled = !enviando) {
                            Text(if (enviando) "Enviando..." else "Sincronizar")
                        }
                    }
                }
            }
            items(turmas, key = { it.id }) { turma ->
                val daTurma = alunos.filter { it.turmaId == turma.id }
                val feitos = daTurma.count { it.presencaHoje != null }
                val alerta = daTurma.count { it.nivel == NivelAlerta.ALERTA }
                val atencao = daTurma.count { it.nivel == NivelAlerta.ATENCAO }

                Card(
                    onClick = { onAbrirTurma(turma.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(turma.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${turma.escola} · ${turma.turno}", style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Abrir turma")
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Chamada de hoje: $feitos de ${daTurma.size}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (daTurma.isEmpty()) 0f else feitos.toFloat() / daTurma.size },
                            modifier = Modifier.fillMaxWidth(),
                            trackColor = MaterialTheme.colorScheme.primaryContainer,
                        )
                        if (alerta + atencao > 0) {
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (alerta > 0) Selo("$alerta em alerta", VermelhoAlerta, VermelhoClaro)
                                if (atencao > 0) Selo("$atencao em atenção", AmbarAtencao, AmbarClaro)
                            }
                        }
                    }
                }
            }
        }
    }
}
