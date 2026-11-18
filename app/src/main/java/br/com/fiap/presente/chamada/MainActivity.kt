package br.com.fiap.presente.chamada

import android.os.Bundle
import androidx.activity.ComponentActivity
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
        enableEdgeToEdge()
        setContent {
            PresenteTheme {
                PresenteNavHost(viewModel)
            }
        }
    }
}
