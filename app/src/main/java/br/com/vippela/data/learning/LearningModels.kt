package br.com.vippela.data.learning

/**
 * Modelos do conteúdo da trilha. O JSON fica em `assets/learning` e é lido
 * uma única vez por sessão; nada aqui depende de rede.
 *
 * Os campos são anuláveis de propósito: o Gson não chama o construtor e
 * campos ausentes no JSON viram `null`, mesmo em tipos não anuláveis.
 */
data class LearningPack(val versao: Int = 1, val trilhas: List<Trilha>? = null)

data class Trilha(
    val slug: String = "",
    val titulo: String = "",
    val pilar: String = "",
    val nivel: String = "",
    val duracaoMin: Int = 0,
    val publico: List<String>? = null,
    val objetivo: String = "",
    val prerequisitos: List<String>? = null,
    val icone: String = "",
    val modulos: List<Modulo>? = null,
) {
    val listaModulos: List<Modulo>
        get() = modulos ?: emptyList()

    val listaLicoes: List<Licao>
        get() = listaModulos.flatMap { it.listaLicoes }

    val listaPrerequisitos: List<String>
        get() = prerequisitos ?: emptyList()
}

data class Modulo(
    val id: String = "",
    val titulo: String = "",
    val objetivo: String = "",
    val duracaoMin: Int = 0,
    val licoes: List<Licao>? = null,
) {
    val listaLicoes: List<Licao>
        get() = licoes ?: emptyList()
}

data class Licao(
    val id: String = "",
    val titulo: String = "",
    val tipo: String = "",
    val duracaoSeg: Int = 0,
    val resumo: String = "",
    val blocos: List<Bloco>? = null,
    val quiz: Quiz? = null,
) {
    val listaBlocos: List<Bloco>
        get() = blocos ?: emptyList()

    val duracaoMin: Int
        get() = if (duracaoSeg > 0) maxOf(1, duracaoSeg / 60) else 3
}

data class Bloco(
    val tipo: String = "texto",
    val titulo: String? = null,
    val texto: String? = null,
    val itens: List<String>? = null,
    val opcoes: List<OpcaoSimulacao>? = null,
) {
    val listaItens: List<String>
        get() = itens ?: emptyList()

    val listaOpcoes: List<OpcaoSimulacao>
        get() = opcoes ?: emptyList()
}

data class OpcaoSimulacao(
    val texto: String = "",
    val resultado: String = "",
    val correta: Boolean = false,
)

data class Quiz(val perguntas: List<Pergunta>? = null) {
    val listaPerguntas: List<Pergunta>
        get() = perguntas ?: emptyList()
}

data class Pergunta(
    val enunciado: String = "",
    val alternativas: List<String>? = null,
    val correta: Int = 0,
    val feedbackOk: String = "",
    val feedbackErro: String = "",
) {
    val listaAlternativas: List<String>
        get() = alternativas ?: emptyList()
}

/** Progresso local, salvo por perfil. */
data class LearningProgress(
    val licoes: MutableMap<String, String> = mutableMapOf(),
    val notas: MutableMap<String, Int> = mutableMapOf(),
    val modulos: MutableMap<String, Int> = mutableMapOf(),
    val selos: MutableMap<String, String> = mutableMapOf(),
    val certificados: MutableMap<String, String> = mutableMapOf(),
    val semanas: MutableSet<String> = mutableSetOf(),
    val desafios: MutableSet<String> = mutableSetOf(),
    var ultimaLicao: String? = null,
    var atividadesFamilia: Int = 0,
)

/** Resposta revisada no resultado do quiz. */
data class RevisaoResposta(
    val enunciado: String,
    val escolhida: String,
    val correta: String,
    val acertou: Boolean,
    val feedback: String,
)

/** Resultado pronto para a tela de resultado. */
data class ResultadoQuiz(
    val titulo: String,
    val alvo: String,
    val acertos: Int,
    val total: Int,
    val aprovado: Boolean,
    val voltarPara: String,
    val continuarPara: String?,
    val revisao: List<RevisaoResposta>,
    val selos: List<Badge> = emptyList(),
    val emitirCertificado: String? = null,
) {
    val percentual: Int
        get() = if (total == 0) 0 else acertos * 100 / total
}

/** Fonte das perguntas de um quiz: uma lição ou uma checagem de módulo. */
data class AlvoQuiz(
    val tipo: TipoAlvo,
    val trilha: Trilha,
    val modulo: Modulo,
    val licao: Licao? = null,
) {
    enum class TipoAlvo { LICAO, MODULO }

    val chave: String
        get() = if (tipo == TipoAlvo.LICAO) "licao:${licao?.id}" else "modulo:${modulo.id}"

    /** Uma pergunta por lição, na ordem do módulo. */
    val perguntas: List<Pergunta>
        get() =
            when (tipo) {
                TipoAlvo.LICAO -> licao?.quiz?.listaPerguntas.orEmpty()
                TipoAlvo.MODULO ->
                    modulo.listaLicoes.mapNotNull { it.quiz?.listaPerguntas?.firstOrNull() }
            }

    val acertosMinimos: Int
        get() = if (tipo == TipoAlvo.LICAO) 2 else 2

    val titulo: String
        get() = if (tipo == TipoAlvo.LICAO) "Quiz da lição" else "Checagem do módulo"
}
