package br.com.fiap.presente.chamada.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.fiap.presente.chamada.data.local.TurmaEntity
import br.com.fiap.presente.chamada.data.remote.ApiSimulada
import br.com.fiap.presente.chamada.data.repository.ChamadaRepository
import br.com.fiap.presente.chamada.data.repository.ResultadoEnvio
import br.com.fiap.presente.chamada.domain.AlunoSituacao
import br.com.fiap.presente.chamada.domain.NivelAlerta
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel compartilhado pelas telas (MVVM).
 * Expõe o estado em StateFlow e recebe as ações do usuário; não conhece nada de Compose.
 */
class PresenteViewModel(
    private val repository: ChamadaRepository,
    private val apiSimulada: ApiSimulada?,
) : ViewModel() {

    val turmas: StateFlow<List<TurmaEntity>> =
        repository.turmas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val alunos: StateFlow<List<AlunoSituacao>> =
        repository.situacoes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pendentes: StateFlow<Int> =
        repository.quantidadePendente.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando.asStateFlow()

    private val _semInternet = MutableStateFlow(false)
    val semInternet: StateFlow<Boolean> = _semInternet.asStateFlow()

    /** Só existe quando o app usa a API simulada. */
    val podeSimularOffline: Boolean = apiSimulada != null

    private val _mensagens = Channel<String>(Channel.BUFFERED)
    val mensagens = _mensagens.receiveAsFlow()

    init {
        viewModelScope.launch { repository.popularSeVazio() }
    }

    fun alunosDaTurma(lista: List<AlunoSituacao>, turmaId: Long, soEmAlerta: Boolean) =
        lista.filter { it.turmaId == turmaId && (!soEmAlerta || it.nivel != NivelAlerta.OK) }

    fun marcarPresenca(alunoId: Long, presente: Boolean) {
        viewModelScope.launch { repository.marcarPresenca(alunoId, presente) }
    }

    /** Marca como presente todos os alunos da turma que ainda não têm chamada hoje. */
    fun marcarRestantesPresentes(turmaId: Long) {
        viewModelScope.launch {
            alunos.value.filter { it.turmaId == turmaId && it.presencaHoje == null }
                .forEach { repository.marcarPresenca(it.id, true) }
        }
    }

    fun sincronizar() = executarEnvio { repository.sincronizar() }

    fun abrirCaso(alunoId: Long) = executarEnvio { repository.abrirCaso(alunoId) }

    fun alternarSemInternet(ativo: Boolean) {
        apiSimulada?.online = !ativo
        _semInternet.value = ativo
    }

    private fun executarEnvio(acao: suspend () -> ResultadoEnvio) {
        if (_enviando.value) return
        viewModelScope.launch {
            _enviando.value = true
            val resultado = acao()
            _enviando.value = false
            _mensagens.send(resultado.mensagem)
        }
    }

    companion object {
        fun fabrica(repository: ChamadaRepository, apiSimulada: ApiSimulada?): ViewModelProvider.Factory =
            viewModelFactory { initializer { PresenteViewModel(repository, apiSimulada) } }
    }
}
