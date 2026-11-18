package br.com.fiap.presente.chamada.data.repository

import br.com.fiap.presente.chamada.data.local.AlunoEntity
import br.com.fiap.presente.chamada.data.local.PresenteDao
import br.com.fiap.presente.chamada.data.local.RegistroPresencaEntity
import br.com.fiap.presente.chamada.data.local.TurmaEntity
import br.com.fiap.presente.chamada.data.mock.DadosMock
import br.com.fiap.presente.chamada.data.remote.AberturaCasoDto
import br.com.fiap.presente.chamada.data.remote.CasoResposta
import br.com.fiap.presente.chamada.data.remote.PresenteApi
import br.com.fiap.presente.chamada.data.remote.RegistroFrequenciaDto
import br.com.fiap.presente.chamada.domain.AlunoSituacao
import br.com.fiap.presente.chamada.domain.RegraGatilho
import br.com.fiap.presente.chamada.domain.StatusCaso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/** Resultado de uma operação que depende da rede. */
sealed interface ResultadoEnvio {
    val mensagem: String

    data class Sucesso(override val mensagem: String) : ResultadoEnvio

    /** Sem conexão ou backend fora do ar: os dados ficam salvos no aparelho para enviar depois. */
    data class SalvoOffline(override val mensagem: String) : ResultadoEnvio
}

/**
 * Fonte única de dados do app, no modelo offline-first:
 * tudo é gravado primeiro no Room e só depois enviado ao backend.
 */
class ChamadaRepository(
    private val dao: PresenteDao,
    private val api: PresenteApi,
    private val hoje: () -> LocalDate = { LocalDate.now() },
) {

    val turmas: Flow<List<TurmaEntity>> = dao.observarTurmas()

    val quantidadePendente: Flow<Int> = dao.observarQuantidadePendente()

    /** Situação de todos os alunos, recalculada sempre que um registro ou caso muda. */
    val situacoes: Flow<List<AlunoSituacao>> =
        combine(dao.observarAlunos(), dao.observarRegistros()) { alunos, registros ->
            val porAluno = registros.groupBy { it.alunoId }
            alunos.map { montarSituacao(it, porAluno[it.id].orEmpty()) }
        }

    /** Na primeira execução, carrega as turmas e o histórico simulados. */
    suspend fun popularSeVazio() {
        if (dao.contarTurmas() > 0) return
        dao.inserirTurmas(DadosMock.turmas)
        dao.inserirAlunos(DadosMock.alunosEntity)
        dao.inserirRegistros(DadosMock.registros(hoje()))
    }

    suspend fun marcarPresenca(alunoId: Long, presente: Boolean) {
        dao.marcarPresenca(alunoId, hoje().toString(), presente)
    }

    /** Envia ao backend as chamadas ainda não sincronizadas e os casos que ficaram pendentes. */
    suspend fun sincronizar(): ResultadoEnvio {
        val pendentes = dao.registrosPendentes()
        val casosPendentes = dao.alunosComStatus(StatusCaso.PENDENTE_ENVIO.name)
        if (pendentes.isEmpty() && casosPendentes.isEmpty()) {
            return ResultadoEnvio.Sucesso("Tudo em dia: nada pendente para enviar.")
        }
        return try {
            if (pendentes.isNotEmpty()) {
                api.enviarFrequencias(pendentes.map { RegistroFrequenciaDto(it.alunoId, it.data, it.presente) })
                dao.marcarSincronizados(pendentes.map { it.id })
            }
            casosPendentes.forEach { enviarCaso(it) }
            val casos = if (casosPendentes.isEmpty()) "" else " e ${casosPendentes.size} caso(s) aberto(s)"
            ResultadoEnvio.Sucesso("Sincronizado: ${pendentes.size} registro(s) de chamada$casos.")
        } catch (e: Exception) {
            ResultadoEnvio.SalvoOffline("Sem conexão com o servidor. Tudo continua salvo no aparelho.")
        }
    }

    /**
     * Abre um caso de busca ativa para o aluno. Grava primeiro como PENDENTE_ENVIO no aparelho;
     * quando o backend confirma, passa a ABERTO.
     */
    suspend fun abrirCaso(alunoId: Long): ResultadoEnvio {
        val aluno = dao.buscarAluno(alunoId)
            ?: return ResultadoEnvio.SalvoOffline("Aluno não encontrado.")
        dao.atualizarStatusCaso(alunoId, StatusCaso.PENDENTE_ENVIO.name)
        return try {
            val resposta = enviarCaso(aluno)
            ResultadoEnvio.Sucesso("${resposta.casoId} aberto. ${resposta.mensagem}")
        } catch (e: Exception) {
            ResultadoEnvio.SalvoOffline("Sem conexão. O caso ficou salvo e será enviado na próxima sincronização.")
        }
    }

    private suspend fun enviarCaso(aluno: AlunoEntity): CasoResposta {
        val resumo = RegraGatilho.resumir(dao.registrosDoAluno(aluno.id).map { it.presente })
        val resposta = api.abrirCaso(
            AberturaCasoDto(
                alunoId = aluno.id,
                turmaId = aluno.turmaId,
                frequencia = resumo.percentual,
                faltasSeguidas = resumo.faltasSeguidas,
                idiomaFamilia = aluno.idiomaFamilia,
            )
        )
        dao.atualizarStatusCaso(aluno.id, StatusCaso.ABERTO.name)
        return resposta
    }

    private fun montarSituacao(aluno: AlunoEntity, registros: List<RegistroPresencaEntity>): AlunoSituacao {
        val presencas = registros.sortedBy { it.data }.map { it.presente }
        val resumo = RegraGatilho.resumir(presencas)
        return AlunoSituacao(
            id = aluno.id,
            nome = aluno.nome,
            turmaId = aluno.turmaId,
            idiomaFamilia = aluno.idiomaFamilia,
            responsavel = aluno.responsavel,
            resumo = resumo,
            nivel = RegraGatilho.avaliar(resumo),
            statusCaso = StatusCaso.valueOf(aluno.statusCaso),
            presencaHoje = registros.firstOrNull { it.data == hoje().toString() }?.presente,
            ultimasAulas = presencas.takeLast(10),
        )
    }
}
