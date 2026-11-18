package br.com.fiap.presente.chamada

import android.app.Application
import br.com.fiap.presente.chamada.data.local.PresenteDatabase
import br.com.fiap.presente.chamada.data.remote.ApiClient
import br.com.fiap.presente.chamada.data.repository.ChamadaRepository

/** Ponto único de criação das dependências (injeção manual, sem framework). */
class PresenteApp : Application() {

    val repository: ChamadaRepository by lazy {
        ChamadaRepository(
            dao = PresenteDatabase.criar(this).dao(),
            api = ApiClient.criar(),
        )
    }
}
