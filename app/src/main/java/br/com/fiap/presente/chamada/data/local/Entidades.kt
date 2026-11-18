package br.com.fiap.presente.chamada.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "turmas")
data class TurmaEntity(
    @PrimaryKey val id: Long,
    val nome: String,
    val escola: String,
    val turno: String,
)

@Entity(
    tableName = "alunos",
    foreignKeys = [ForeignKey(entity = TurmaEntity::class, parentColumns = ["id"], childColumns = ["turmaId"])],
    indices = [Index("turmaId")],
)
data class AlunoEntity(
    @PrimaryKey val id: Long,
    val turmaId: Long,
    val nome: String,
    val responsavel: String,
    /** Idioma em que o bot conversa com a família (pt, es, ht...). */
    val idiomaFamilia: String,
    /** Nome de um [br.com.fiap.presente.chamada.domain.StatusCaso]. */
    val statusCaso: String = "SEM_CASO",
)

/**
 * Um registro de presença/falta. É salvo primeiro no aparelho (offline-first);
 * [sincronizado] vira true quando o backend confirma o recebimento.
 */
@Entity(
    tableName = "registros_presenca",
    foreignKeys = [ForeignKey(entity = AlunoEntity::class, parentColumns = ["id"], childColumns = ["alunoId"])],
    indices = [Index(value = ["alunoId", "data"], unique = true)],
)
data class RegistroPresencaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alunoId: Long,
    /** Data da aula no formato ISO (yyyy-MM-dd). */
    val data: String,
    val presente: Boolean,
    val sincronizado: Boolean = false,
)
