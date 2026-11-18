package br.com.fiap.presente.chamada.domain

/** Situação de frequência do aluno, calculada pela [RegraGatilho]. */
enum class NivelAlerta { OK, ATENCAO, ALERTA }

/** Situação do caso de busca ativa do aluno. */
enum class StatusCaso {
    /** Nenhum caso aberto. */
    SEM_CASO,

    /** Gatilho atingido e caso criado no aparelho, aguardando envio ao backend (offline). */
    PENDENTE_ENVIO,

    /** Backend confirmou: caso aberto e conversa com a família disparada no WhatsApp. */
    ABERTO,
}

/** Resumo de frequência de um aluno. */
data class ResumoFrequencia(
    val totalAulas: Int,
    val presencas: Int,
    val faltas: Int,
    val faltasSeguidas: Int,
) {
    /** Frequência de 0.0 a 1.0. Sem aulas registradas, considera 100%. */
    val percentual: Double
        get() = if (totalAulas == 0) 1.0 else presencas.toDouble() / totalAulas
}

/** Tudo o que as telas precisam saber sobre um aluno. */
data class AlunoSituacao(
    val id: Long,
    val nome: String,
    val turmaId: Long,
    val idiomaFamilia: String,
    val responsavel: String,
    val resumo: ResumoFrequencia,
    val nivel: NivelAlerta,
    val statusCaso: StatusCaso,
    /** Presença na aula de hoje: true/false, ou null se a chamada ainda não foi feita. */
    val presencaHoje: Boolean?,
    /** Últimas aulas, da mais antiga para a mais recente (true = presente). */
    val ultimasAulas: List<Boolean>,
)
