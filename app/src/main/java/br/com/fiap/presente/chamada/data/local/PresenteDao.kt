package br.com.fiap.presente.chamada.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresenteDao {

    @Query("SELECT * FROM turmas ORDER BY nome")
    fun observarTurmas(): Flow<List<TurmaEntity>>

    @Query("SELECT * FROM turmas WHERE id = :id")
    suspend fun buscarTurma(id: Long): TurmaEntity?

    @Query("SELECT * FROM alunos ORDER BY nome")
    fun observarAlunos(): Flow<List<AlunoEntity>>

    @Query("SELECT * FROM alunos WHERE id = :id")
    suspend fun buscarAluno(id: Long): AlunoEntity?

    @Query("SELECT * FROM alunos WHERE statusCaso = :status")
    suspend fun alunosComStatus(status: String): List<AlunoEntity>

    @Query("SELECT * FROM registros_presenca ORDER BY data")
    fun observarRegistros(): Flow<List<RegistroPresencaEntity>>

    @Query("SELECT * FROM registros_presenca WHERE alunoId = :alunoId ORDER BY data")
    suspend fun registrosDoAluno(alunoId: Long): List<RegistroPresencaEntity>

    @Query("SELECT * FROM registros_presenca WHERE sincronizado = 0 ORDER BY data")
    suspend fun registrosPendentes(): List<RegistroPresencaEntity>

    @Query("SELECT COUNT(*) FROM registros_presenca WHERE sincronizado = 0")
    fun observarQuantidadePendente(): Flow<Int>

    @Query("SELECT COUNT(*) FROM turmas")
    suspend fun contarTurmas(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserirTurmas(turmas: List<TurmaEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserirAlunos(alunos: List<AlunoEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserirRegistros(registros: List<RegistroPresencaEntity>)

    /** Marca a chamada do dia. Se o aluno já tem registro na data, substitui e volta a ficar pendente. */
    @Query(
        """INSERT OR REPLACE INTO registros_presenca (id, alunoId, data, presente, sincronizado)
           VALUES ((SELECT id FROM registros_presenca WHERE alunoId = :alunoId AND data = :data),
                   :alunoId, :data, :presente, 0)"""
    )
    suspend fun marcarPresenca(alunoId: Long, data: String, presente: Boolean)

    @Query("UPDATE registros_presenca SET sincronizado = 1 WHERE id IN (:ids)")
    suspend fun marcarSincronizados(ids: List<Long>)

    @Query("UPDATE alunos SET statusCaso = :status WHERE id = :alunoId")
    suspend fun atualizarStatusCaso(alunoId: Long, status: String)
}
