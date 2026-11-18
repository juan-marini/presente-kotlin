package br.com.fiap.presente.chamada

import android.app.Application
import br.com.fiap.presente.chamada.data.local.PresenteDatabase
import br.com.fiap.presente.chamada.data.remote.ApiClient
import br.com.fiap.presente.chamada.data.remote.ApiSimulada
import br.com.fiap.presente.chamada.data.repository.ChamadaRepository

/** Ponto único de criação das dependências (injeção manual, sem framework). */
class PresenteApp : Application() {

    val api by lazy { ApiClient.criar() }

    /** Disponível só no modo simulado; permite demonstrar o app sem internet. */
    val apiSimulada: ApiSimulada? get() = api as? ApiSimulada

    val repository: ChamadaRepository by lazy {
        ChamadaRepository(dao = PresenteDatabase.criar(this).dao(), api = api)
    }
}
