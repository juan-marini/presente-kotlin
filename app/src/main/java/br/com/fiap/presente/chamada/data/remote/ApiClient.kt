package br.com.fiap.presente.chamada.data.remote

import br.com.fiap.presente.chamada.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    /** Escolhe entre a API simulada e a real (Retrofit + API Gateway), conforme o build. */
    fun criar(): PresenteApi =
        if (BuildConfig.USAR_API_SIMULADA) {
            ApiSimulada()
        } else {
            Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PresenteApi::class.java)
        }
}
