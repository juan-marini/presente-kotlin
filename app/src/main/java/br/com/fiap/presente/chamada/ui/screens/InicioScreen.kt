package br.com.fiap.presente.chamada.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.presente.chamada.domain.NivelAlerta
import br.com.fiap.presente.chamada.ui.PresenteViewModel
import br.com.fiap.presente.chamada.ui.theme.VerdeEscuro
import br.com.fiap.presente.chamada.ui.theme.VerdePresente

/** Tela inicial: nome, objetivo do app, como funciona e atalhos. */
@Composable
fun InicioScreen(
    viewModel: PresenteViewModel,
    snackbar: SnackbarHostState,
    onFazerChamada: () -> Unit,
    onVerAlertas: () -> Unit,
) {
    val alunos by viewModel.alunos.collectAsStateWithLifecycle()
    val semInternet by viewModel.semInternet.collectAsStateWithLifecycle()
    val emAlerta = alunos.count { it.nivel == NivelAlerta.ALERTA }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = padding.calculateBottomPadding()),
        ) {
            // Cabeçalho com a identidade visual
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(VerdePresente)
                    .statusBarsPadding()
                    .padding(24.dp),
            ) {
                Box(
                    Modifier
                        .size(56.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.CheckCircle, null, tint = VerdePresente, modifier = Modifier.size(40.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("Presente!", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                Text("Chamada", color = Color.White.copy(alpha = 0.85f), fontSize = 20.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "A chamada que vira cuidado: o professor registra a falta em sala e, quando o aluno " +
                        "entra em risco de evasão, o Presente! abre um caso de busca ativa e conversa com a família.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onFazerChamada, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Filled.EditNote, null)
                    Spacer(Modifier.size(8.dp))
                    Text("Fazer chamada", fontSize = 16.sp)
                }
                OutlinedButton(onClick = onVerAlertas, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Filled.NotificationsActive, null)
                    Spacer(Modifier.size(8.dp))
                    Text(if (emAlerta > 0) "Ver alunos em alerta ($emAlerta)" else "Ver alunos em alerta", fontSize = 16.sp)
                }

                Text(
                    "Como funciona",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                PassoCard(Icons.Filled.EditNote, "1. Chamada com um toque", "Presente ou falta, mesmo sem internet: tudo fica salvo no aparelho.")
                PassoCard(Icons.Filled.NotificationsActive, "2. Gatilho de risco", "3 faltas seguidas ou frequência abaixo de 80% (mínimo do Pé-de-Meia) acendem o alerta.")
                PassoCard(Icons.Filled.ChatBubble, "3. Conversa, não só aviso", "O caso vai para o backend e o assistente conversa com a família no WhatsApp, no idioma dela.")
                PassoCard(Icons.Filled.Groups, "4. Rede de apoio", "O agente de busca ativa recebe o motivo e encaminha: CRAS, UBS, Conselho Tutelar, passe livre.")

                if (viewModel.podeSimularOffline) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WifiOff, null, tint = VerdeEscuro)
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text("Simular escola sem internet", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Para demonstrar o modo offline-first.",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Switch(checked = semInternet, onCheckedChange = viewModel::alternarSemInternet)
                        }
                    }
                }
                Text(
                    "GS 2026.2 · CitizenTech · ODS 4 e 10",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PassoCard(icone: ImageVector, titulo: String, texto: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icone, null, tint = VerdeEscuro)
            }
            Column(Modifier.padding(start = 12.dp)) {
                Text(titulo, fontWeight = FontWeight.SemiBold)
                Text(texto, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
