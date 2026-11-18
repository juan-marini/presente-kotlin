package br.com.fiap.presente.chamada

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import br.com.fiap.presente.chamada.ui.PresenteViewModel
import br.com.fiap.presente.chamada.ui.navigation.PresenteNavHost
import br.com.fiap.presente.chamada.ui.theme.PresenteTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PresenteViewModel by viewModels {
        val app = application as PresenteApp
        PresenteViewModel.fabrica(app.repository, app.apiSimulada)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barra de status com ícones claros, sobre o verde do cabeçalho.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            PresenteTheme {
                PresenteNavHost(viewModel)
            }
        }
    }
}
