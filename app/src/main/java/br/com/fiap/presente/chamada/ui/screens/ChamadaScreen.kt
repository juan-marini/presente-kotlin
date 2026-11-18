package br.com.fiap.presente.chamada.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.presente.chamada.domain.AlunoSituacao
import br.com.fiap.presente.chamada.domain.StatusCaso
import br.com.fiap.presente.chamada.ui.PresenteViewModel
import br.com.fiap.presente.chamada.ui.components.SeloNivel
import br.com.fiap.presente.chamada.ui.components.TelaPadrao
import br.com.fiap.presente.chamada.ui.components.formatarPercentual
import br.com.fiap.presente.chamada.ui.theme.VerdePresente
import br.com.fiap.presente.chamada.ui.theme.VermelhoAlerta

/** Chamada da turma: um toque marca presente ou falta; filtro mostra só quem está em risco. */
@Composable
fun ChamadaScreen(
    turmaId: Long,
    viewModel: PresenteViewModel,
    snackbar: SnackbarHostState,
    onVoltar: () -> Unit,
    onAbrirAluno: (Long) -> Unit,
) {
    val turmas by viewModel.turmas.collectAsStateWithLifecycle()
    val todos by viewModel.alunos.collectAsStateWithLifecycle()
    val enviando by viewModel.enviando.collectAsStateWithLifecycle()
    var soEmAlerta by rememberSaveable { mutableStateOf(false) }

    val turma = turmas.firstOrNull { it.id == turmaId }
    val daTurma = viewModel.alunosDaTurma(todos, turmaId, soEmAlerta = false)
    val visiveis = viewModel.alunosDaTurma(todos, turmaId, soEmAlerta)
    val presentes = daTurma.count { it.presencaHoje == true }
    val faltas = daTurma.count { it.presencaHoje == false }
    val semMarcar = daTurma.size - presentes - faltas

    TelaPadrao(
        titulo = turma?.nome ?: "Chamada",
        subtitulo = "Chamada de hoje",
        snackbar = snackbar,
        onVoltar = onVoltar,
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { viewModel.marcarRestantesPresentes(turmaId) },
                        enabled = semMarcar > 0,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.DoneAll, null)
                        Spacer(Modifier.size(6.dp))
                        Text("Demais presentes")
                    }
                    Button(onClick = viewModel::sincronizar, enabled = !enviando, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.CloudUpload, null)
                        Spacer(Modifier.size(6.dp))
                        Text(if (enviando) "Enviando..." else "Enviar chamada")
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Contador("Presentes", presentes, VerdePresente, Modifier.weight(1f))
                    Contador("Faltas", faltas, VermelhoAlerta, Modifier.weight(1f))
                    Contador("A marcar", semMarcar, MaterialTheme.colorScheme.onBackground, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                FilterChip(
                    selected = soEmAlerta,
                    onClick = { soEmAlerta = !soEmAlerta },
                    label = { Text("Mostrar só alunos em risco") },
                )
            }
            if (visiveis.isEmpty()) {
                item {
                    Text(
                        "Nenhum aluno em risco nesta turma.",
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            items(visiveis, key = { it.id }) { aluno ->
                LinhaChamada(
                    aluno = aluno,
                    onPresenca = { viewModel.marcarPresenca(aluno.id, it) },
                    onAbrir = { onAbrirAluno(aluno.id) },
                )
            }
        }
    }
}

@Composable
private fun Contador(rotulo: String, valor: Int, cor: Color, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$valor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = cor)
            Text(rotulo, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun LinhaChamada(aluno: AlunoSituacao, onPresenca: (Boolean) -> Unit, onAbrir: () -> Unit) {
    Card(
        onClick = onAbrir,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(aluno.nome, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SeloNivel(aluno.nivel)
                    Text(
                        "Freq. ${formatarPercentual(aluno.resumo.percentual)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (aluno.statusCaso != StatusCaso.SEM_CASO) {
                        Text("· caso", style = MaterialTheme.typography.bodySmall, color = VermelhoAlerta)
                    }
                }
            }
            BotaoPresenca(
                marcado = aluno.presencaHoje == true,
                cor = VerdePresente,
                descricao = "Presente",
                icone = { Icon(Icons.Filled.Check, "Marcar presente") },
                onClick = { onPresenca(true) },
            )
            Spacer(Modifier.size(6.dp))
            BotaoPresenca(
                marcado = aluno.presencaHoje == false,
                cor = VermelhoAlerta,
                descricao = "Falta",
                icone = { Icon(Icons.Filled.Close, "Marcar falta") },
                onClick = { onPresenca(false) },
            )
        }
    }
}

@Composable
private fun BotaoPresenca(
    marcado: Boolean,
    cor: Color,
    descricao: String,
    icone: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconToggleButton(
            checked = marcado,
            onCheckedChange = { onClick() },
            colors = IconButtonDefaults.filledIconToggleButtonColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = cor,
                checkedContainerColor = cor,
                checkedContentColor = Color.White,
            ),
        ) { icone() }
        Text(descricao, style = MaterialTheme.typography.labelSmall)
    }
}
