package br.com.fiap.presente.chamada.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.fiap.presente.chamada.ui.PresenteViewModel
import br.com.fiap.presente.chamada.ui.screens.AlertasScreen
import br.com.fiap.presente.chamada.ui.screens.AlunoScreen
import br.com.fiap.presente.chamada.ui.screens.ChamadaScreen
import br.com.fiap.presente.chamada.ui.screens.InicioScreen
import br.com.fiap.presente.chamada.ui.screens.TurmasScreen

/** Rotas do app. Rotas com argumento recebem o id na própria URL da rota. */
object Rotas {
    const val INICIO = "inicio"
    const val TURMAS = "turmas"
    const val ALERTAS = "alertas"
    const val CHAMADA = "chamada/{turmaId}"
    const val ALUNO = "aluno/{alunoId}"

    fun chamada(turmaId: Long) = "chamada/$turmaId"
    fun aluno(alunoId: Long) = "aluno/$alunoId"
}

@Composable
fun PresenteNavHost(viewModel: PresenteViewModel) {
    val navController = rememberNavController()
    val snackbar = remember { SnackbarHostState() }

    // Mensagens do ViewModel (sincronização, abertura de caso) aparecem em qualquer tela.
    LaunchedEffect(Unit) {
        viewModel.mensagens.collect { snackbar.showSnackbar(it) }
    }

    val voltar: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Rotas.INICIO) {
        composable(Rotas.INICIO) {
            InicioScreen(
                viewModel = viewModel,
                snackbar = snackbar,
                onFazerChamada = { navController.navigate(Rotas.TURMAS) },
                onVerAlertas = { navController.navigate(Rotas.ALERTAS) },
            )
        }
        composable(Rotas.TURMAS) {
            TurmasScreen(
                viewModel = viewModel,
                snackbar = snackbar,
                onVoltar = voltar,
                onAbrirTurma = { navController.navigate(Rotas.chamada(it)) },
            )
        }
        composable(
            Rotas.CHAMADA,
            arguments = listOf(navArgument("turmaId") { type = NavType.LongType }),
        ) { entry ->
            ChamadaScreen(
                turmaId = entry.arguments?.getLong("turmaId") ?: 0L,
                viewModel = viewModel,
                snackbar = snackbar,
                onVoltar = voltar,
                onAbrirAluno = { navController.navigate(Rotas.aluno(it)) },
            )
        }
        composable(
            Rotas.ALUNO,
            arguments = listOf(navArgument("alunoId") { type = NavType.LongType }),
        ) { entry ->
            AlunoScreen(
                alunoId = entry.arguments?.getLong("alunoId") ?: 0L,
                viewModel = viewModel,
                snackbar = snackbar,
                onVoltar = voltar,
            )
        }
        composable(Rotas.ALERTAS) {
            AlertasScreen(
                viewModel = viewModel,
                snackbar = snackbar,
                onVoltar = voltar,
                onAbrirAluno = { navController.navigate(Rotas.aluno(it)) },
            )
        }
    }
}
