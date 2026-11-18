package br.com.fiap.presente.chamada.domain

/**
 * Regra que decide quando a falta deixa de ser "só um número" e vira um caso de busca ativa.
 *
 * - ALERTA: 3 ou mais faltas seguidas, ou frequência abaixo de 80% (mínimo exigido pelo Pé-de-Meia).
 *   Ao atingir ALERTA, o app oferece abrir o caso no backend.
 * - ATENÇÃO: 2 faltas seguidas, ou frequência abaixo de 85% (chegando perto dos 80%).
 * - OK: demais casos.
 *
 * Fica num objeto puro (sem Android) para ser fácil de testar e de alinhar com o backend.
 */
object RegraGatilho {
    const val FREQUENCIA_MINIMA = 0.80
    const val FREQUENCIA_ATENCAO = 0.85
    const val FALTAS_SEGUIDAS_ALERTA = 3
    const val FALTAS_SEGUIDAS_ATENCAO = 2

    fun avaliar(resumo: ResumoFrequencia): NivelAlerta = when {
        resumo.faltasSeguidas >= FALTAS_SEGUIDAS_ALERTA -> NivelAlerta.ALERTA
        resumo.percentual < FREQUENCIA_MINIMA -> NivelAlerta.ALERTA
        resumo.faltasSeguidas >= FALTAS_SEGUIDAS_ATENCAO -> NivelAlerta.ATENCAO
        resumo.percentual < FREQUENCIA_ATENCAO -> NivelAlerta.ATENCAO
        else -> NivelAlerta.OK
    }

    /**
     * Monta o resumo a partir das presenças em ordem cronológica (true = presente).
     * Faltas seguidas = faltas consecutivas mais recentes.
     */
    fun resumir(presencasEmOrdem: List<Boolean>): ResumoFrequencia {
        val presencas = presencasEmOrdem.count { it }
        val seguidas = presencasEmOrdem.asReversed().takeWhile { !it }.size
        return ResumoFrequencia(
            totalAulas = presencasEmOrdem.size,
            presencas = presencas,
            faltas = presencasEmOrdem.size - presencas,
            faltasSeguidas = seguidas,
        )
    }
}
