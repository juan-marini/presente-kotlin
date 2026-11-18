package br.com.fiap.presente.chamada.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Contrato REST com o API Gateway dos microsserviços do Presente!.
 * - POST /api/frequencias -> ms-alunos recebe a chamada do dia
 * - POST /api/casos       -> ms-casos abre o caso de busca ativa e dispara o bot no WhatsApp
 *
 * As respostas chegam em JSON e o Gson converte para as data classes abaixo.
 * Respostas HTTP fora da faixa 2xx viram exceção e o repositório trata como "salvo offline".
 */
interface PresenteApi {

    @POST("api/frequencias")
    suspend fun enviarFrequencias(@Body registros: List<RegistroFrequenciaDto>): EnvioFrequenciaResposta

    @POST("api/casos")
    suspend fun abrirCaso(@Body pedido: AberturaCasoDto): CasoResposta
}

data class RegistroFrequenciaDto(
    val alunoId: Long,
    val data: String,
    val presente: Boolean,
)

data class EnvioFrequenciaResposta(
    val recebidos: Int,
)

data class AberturaCasoDto(
    val alunoId: Long,
    val turmaId: Long,
    val frequencia: Double,
    val faltasSeguidas: Int,
    val idiomaFamilia: String,
    val origem: String = "app-chamada",
)

data class CasoResposta(
    val casoId: String,
    val status: String,
    val mensagem: String,
)
