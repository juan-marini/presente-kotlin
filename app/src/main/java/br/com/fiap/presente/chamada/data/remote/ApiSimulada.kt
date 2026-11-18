package br.com.fiap.presente.chamada.data.remote

import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Implementação simulada do [PresenteApi], usada quando o gateway não está no ar
 * (BuildConfig.USAR_API_SIMULADA = true). Responde como o backend real responderia,
 * com um pequeno atraso de rede, para a demonstração funcionar sem servidor.
 *
 * [online] permite simular a escola sem internet: com false, as chamadas falham
 * e o app mostra o comportamento offline-first.
 */
class ApiSimulada(var online: Boolean = true) : PresenteApi {

    override suspend fun enviarFrequencias(registros: List<RegistroFrequenciaDto>): EnvioFrequenciaResposta {
        delay(600)
        verificarConexao()
        return EnvioFrequenciaResposta(recebidos = registros.size)
    }

    override suspend fun abrirCaso(pedido: AberturaCasoDto): CasoResposta {
        delay(900)
        verificarConexao()
        val idioma = when (pedido.idiomaFamilia) {
            "es" -> "espanhol"
            "ht" -> "crioulo haitiano"
            else -> "português"
        }
        return CasoResposta(
            casoId = "CASO-" + UUID.randomUUID().toString().take(6).uppercase(),
            status = "ABERTO",
            mensagem = "Conversa com a família iniciada no WhatsApp em $idioma.",
        )
    }

    private fun verificarConexao() {
        if (!online) throw java.io.IOException("Sem conexão (simulado)")
    }
}
