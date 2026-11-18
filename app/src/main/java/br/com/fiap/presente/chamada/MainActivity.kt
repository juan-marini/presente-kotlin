package br.com.fiap.presente.chamada

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import br.com.fiap.presente.chamada.ui.theme.PresenteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PresenteTheme {
                Text("Presente! Chamada")
            }
        }
    }
}
