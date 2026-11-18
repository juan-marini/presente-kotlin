package br.com.fiap.presente.chamada.data.mock

import br.com.fiap.presente.chamada.data.local.AlunoEntity
import br.com.fiap.presente.chamada.data.local.RegistroPresencaEntity
import br.com.fiap.presente.chamada.data.local.TurmaEntity
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Dados simulados do diário de classe, carregados na primeira execução do app.
 * Substituem o CSV usado hoje nas outras entregas; com o backend ligado, viriam do ms-alunos.
 *
 * Cada aluno tem um padrão de presença nas últimas 20 aulas ("P" = presente, "F" = falta,
 * da mais antiga para a mais recente), pensado para mostrar os três níveis do gatilho.
 * O comentário indica o motivo que a conversa no WhatsApp revelaria.
 */
object DadosMock {

    val turmas = listOf(
        TurmaEntity(1, "1º A - Ensino Médio", "E.E. Prof.ª Maria Lúcia", "Manhã"),
        TurmaEntity(2, "2º B - Ensino Médio", "E.E. Prof.ª Maria Lúcia", "Tarde"),
        TurmaEntity(3, "3º C - Ensino Médio", "E.E. Prof.ª Maria Lúcia", "Noite"),
    )

    private class AlunoMock(
        val id: Long,
        val turmaId: Long,
        val nome: String,
        val responsavel: String,
        val idioma: String,
        val padrao: String,
    )

    private val alunos = listOf(
        // 1º A
        AlunoMock(101, 1, "Ana Beatriz Souza", "Cláudia Souza", "pt", "PPPPPPPPPPPPPPPPPPPP"),
        AlunoMock(102, 1, "Carlos Eduardo Lima", "Roberto Lima", "pt", "PPPPPPPPPPPPPPPPPFFF"), // trabalho
        AlunoMock(103, 1, "Daniela Rodríguez", "María Rodríguez", "es", "PPPPPPPPPPPPPPPPPPFF"), // cuida do irmão
        AlunoMock(104, 1, "Felipe Andrade", "Simone Andrade", "pt", "PPPPPPPPPFPPPPPPPPPP"),
        AlunoMock(105, 1, "Gabriela Nunes", "Paulo Nunes", "pt", "PPPPPPPPPPPPPPPPPPPP"),
        AlunoMock(106, 1, "Jean-Pierre Joseph", "Marie Joseph", "ht", "PFPPFPPFPPFPPPFPPPFP"), // transporte
        AlunoMock(107, 1, "Larissa Mendes", "Fátima Mendes", "pt", "PPPPPPPPPPPPPPFPPPPP"),
        // 2º B
        AlunoMock(201, 2, "Bruno Henrique Costa", "Luciana Costa", "pt", "PPPPPPPPPPPPPPPPPPPP"),
        AlunoMock(202, 2, "Isabela Ferreira", "Marcos Ferreira", "pt", "PPPPPPFPPPPFPPPPFPPP"),
        AlunoMock(203, 2, "José Miguel Pérez", "Carmen Pérez", "es", "PPPPPPPPPPPPPPPFFFFF"), // mudança de endereço
        AlunoMock(204, 2, "Kauã Oliveira", "Rosa Oliveira", "pt", "PPPPPPPPPPPPPPPPPPPP"),
        AlunoMock(205, 2, "Letícia Ramos", "Sandra Ramos", "pt", "PFPPPPFPPPPPFPPPPPFP"), // saúde
        AlunoMock(206, 2, "Mateus Almeida", "Jorge Almeida", "pt", "PPPPPPPPPPPPPPPPPPPP"),
        // 3º C
        AlunoMock(301, 3, "Nicole Santos", "Patrícia Santos", "pt", "PPPPPPPPPPPPPPPPPPPP"),
        AlunoMock(302, 3, "Pedro Gonçalves", "Eliana Gonçalves", "pt", "PFFPPFPFPPFPPFPPFPFP"), // trabalho noturno
        AlunoMock(303, 3, "Rafaela Dias", "Antônio Dias", "pt", "PPPPPPPPPPPPPPPPPPPF"),
        AlunoMock(304, 3, "Samuel Batista", "Vera Batista", "pt", "PPPPPPPPPPPPPPPPPPPP"),
        AlunoMock(305, 3, "Wislande Pierre", "Rose Pierre", "ht", "PPPPPPPPPPPPPPPPPFFF"), // gravidez
    )

    val alunosEntity: List<AlunoEntity> = alunos.map {
        AlunoEntity(id = it.id, turmaId = it.turmaId, nome = it.nome, responsavel = it.responsavel, idiomaFamilia = it.idioma)
    }

    /** Gera o histórico nas últimas aulas (dias úteis) antes de [hoje]. */
    fun registros(hoje: LocalDate): List<RegistroPresencaEntity> {
        val dias = diasLetivosAntes(hoje, alunos.maxOf { it.padrao.length })
        return alunos.flatMap { aluno ->
            aluno.padrao.mapIndexed { i, c ->
                RegistroPresencaEntity(
                    alunoId = aluno.id,
                    data = dias[i].toString(),
                    presente = c == 'P',
                    sincronizado = true, // histórico já veio do diário oficial
                )
            }
        }
    }

    private fun diasLetivosAntes(hoje: LocalDate, quantidade: Int): List<LocalDate> {
        val dias = mutableListOf<LocalDate>()
        var d = hoje.minusDays(1)
        while (dias.size < quantidade) {
            if (d.dayOfWeek != DayOfWeek.SATURDAY && d.dayOfWeek != DayOfWeek.SUNDAY) dias.add(d)
            d = d.minusDays(1)
        }
        return dias.reversed()
    }
}
