package br.com.fiap.presente.chamada

import br.com.fiap.presente.chamada.domain.NivelAlerta
import br.com.fiap.presente.chamada.domain.RegraGatilho
import org.junit.Assert.assertEquals
import org.junit.Test

class RegraGatilhoTest {

    /** Converte "PPF..." em lista de presenças (P = presente). */
    private fun aulas(padrao: String) = padrao.map { it == 'P' }

    private fun nivel(padrao: String) = RegraGatilho.avaliar(RegraGatilho.resumir(aulas(padrao)))

    @Test
    fun `resumo conta presencas, faltas e faltas seguidas mais recentes`() {
        val resumo = RegraGatilho.resumir(aulas("PFPPFF"))
        assertEquals(6, resumo.totalAulas)
        assertEquals(3, resumo.presencas)
        assertEquals(3, resumo.faltas)
        assertEquals(2, resumo.faltasSeguidas)
        assertEquals(0.5, resumo.percentual, 0.0001)
    }

    @Test
    fun `sem aulas registradas a frequencia e 100 por cento e nivel OK`() {
        assertEquals(1.0, RegraGatilho.resumir(emptyList()).percentual, 0.0001)
        assertEquals(NivelAlerta.OK, nivel(""))
    }

    @Test
    fun `frequencia alta e sem faltas seguidas fica OK`() {
        assertEquals(NivelAlerta.OK, nivel("PPPPPPPPPFPPPPPPPPPP")) // 95%
    }

    @Test
    fun `tres faltas seguidas disparam ALERTA mesmo com frequencia alta`() {
        assertEquals(NivelAlerta.ALERTA, nivel("PPPPPPPPPPPPPPPPPFFF")) // 85%, 3 seguidas
    }

    @Test
    fun `frequencia abaixo de 80 por cento dispara ALERTA`() {
        assertEquals(NivelAlerta.ALERTA, nivel("PFPPFPPFPPFPPPFPPPFP")) // 70%
    }

    @Test
    fun `exatamente 80 por cento nao e ALERTA, e ATENCAO`() {
        assertEquals(NivelAlerta.ATENCAO, nivel("FFFFPPPPPPPPPPPPPPPP")) // 16 de 20 = 80%
    }

    @Test
    fun `duas faltas seguidas ficam em ATENCAO`() {
        assertEquals(NivelAlerta.ATENCAO, nivel("PPPPPPPPPPPPPPPPPPFF")) // 90%, 2 seguidas
    }

    @Test
    fun `presenca hoje zera as faltas seguidas`() {
        assertEquals(0, RegraGatilho.resumir(aulas("PPFFFP")).faltasSeguidas)
    }
}
