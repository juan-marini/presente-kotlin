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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.presente.chamada.domain.NivelAlerta
import br.com.fiap.presente.chamada.domain.StatusCaso
import br.com.fiap.presente.chamada.ui.PresenteViewModel
import br.com.fiap.presente.chamada.ui.components.HistoricoAulas
import br.com.fiap.presente.chamada.ui.components.SeloNivel
import br.com.fiap.presente.chamada.ui.components.TelaPadrao
import br.com.fiap.presente.chamada.ui.components.formatarPercentual
import br.com.fiap.presente.chamada.ui.components.textoDoCaso

/** Todos os alunos em risco, de todas as turmas, do mais grave para o menos grave. */
@Composable
fun AlertasScreen(
    viewModel: PresenteViewModel,
    snackbar: SnackbarHostState,
    onVoltar: () -> Unit,
    onAbrirAluno: (Long) -> Unit,
) {
    val alunos by viewModel.alunos.collectAsStateWithLifecycle()
    val turmas by viewModel.turmas.collectAsStateWithLifecycle()
    var turmaFiltro by rememberSaveable { mutableStateOf<Long?>(null) }

    val emRisco = alunos
        .filter { it.nivel != NivelAlerta.OK && (turmaFiltro == null || it.turmaId == turmaFiltro) }
        .sortedWith(compareByDescending<br.com.fiap.presente.chamada.domain.AlunoSituacao> { it.nivel }.thenBy { it.resumo.percentual })

    TelaPadrao(titulo = "Alunos em risco", subtitulo = "${emRisco.size} aluno(s)", snackbar = snackbar, onVoltar = onVoltar) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = turmaFiltro == null, onClick = { turmaFiltro = null }, label = { Text("Todas") })
                    }
                    items(turmas, key = { it.id }) { turma ->
                        FilterChip(
                            selected = turmaFiltro == turma.id,
                            onClick = { turmaFiltro = turma.id },
                            label = { Text(turma.nome.substringBefore(" -")) },
                        )
                    }
                }
            }
            if (emRisco.isEmpty()) {
                item { Text("Nenhum aluno em risco. 🎉", modifier = Modifier.padding(24.dp)) }
            }
            items(emRisco, key = { it.id }) { aluno ->
                val turma = turmas.firstOrNull { it.id == aluno.turmaId }
                Card(
                    onClick = { onAbrirAluno(aluno.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(aluno.nome, fontWeight = FontWeight.SemiBold)
                                Text(turma?.nome ?: "", style = MaterialTheme.typography.bodySmall)
                            }
                            SeloNivel(aluno.nivel)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Freq. ${formatarPercentual(aluno.resumo.percentual)} · ${aluno.resumo.faltasSeguidas} falta(s) seguida(s)" +
                                if (aluno.statusCaso != StatusCaso.SEM_CASO) " · ${textoDoCaso(aluno.statusCaso)}" else "",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(Modifier.height(6.dp))
                        HistoricoAulas(aluno.ultimasAulas)
                    }
                }
            }
        }
    }
}
