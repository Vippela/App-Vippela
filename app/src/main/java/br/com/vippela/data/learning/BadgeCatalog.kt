package br.com.vippela.data.learning

enum class BadgeTier(val petalas: Int) {
    PRIMEIRO_PASSO(1),
    TRILHA(3),
    PERCURSO(5),
}

/**
 * Catálogo de selos. A arte é desenhada em Compose (medalhão de pétalas),
 * então não há dependência de imagens nesta primeira versão.
 *
 * [disponivel] indica se o selo já pode ser conquistado com as trilhas
 * publicadas. Os demais aparecem como "em breve", sem frustrar quem joga.
 */
data class Badge(
    val codigo: String,
    val nome: String,
    val descricao: String,
    val criterio: String,
    val cor: Long,
    val icone: String,
    val tier: BadgeTier = BadgeTier.TRILHA,
    val disponivel: Boolean = true,
)

object BadgeCatalog {
    const val PEONIA = 0xFFE7A6BE
    const val LAVANDA = 0xFFB9A7E6
    const val VERDE = 0xFF7FB89A
    const val AMBAR = 0xFFE9B872
    const val VIOLETA = 0xFF8047BB

    val todos: List<Badge> =
        listOf(
            Badge(
                codigo = "primeira_petala",
                nome = "Primeira Pétala",
                descricao = "Todo jardim começa com uma pétala.",
                criterio = "Concluir a primeira lição",
                cor = PEONIA,
                icone = "petala",
                tier = BadgeTier.PRIMEIRO_PASSO,
            ),
            Badge(
                codigo = "raiz_forte",
                nome = "Raiz Forte",
                descricao = "Suas contas têm raízes firmes.",
                criterio = "Concluir a trilha Contas e senhas",
                cor = LAVANDA,
                icone = "chave",
            ),
            Badge(
                codigo = "olho_atento",
                nome = "Olho Atento",
                descricao = "Você reconhece o roteiro do golpe antes do clique.",
                criterio = "Concluir a trilha Golpes e fraudes",
                cor = PEONIA,
                icone = "olho",
            ),
            Badge(
                codigo = "ponte_confianca",
                nome = "Ponte de Confiança",
                descricao = "O que se aprende junto fica.",
                criterio = "Concluir 2 lições em família",
                cor = AMBAR,
                icone = "maos",
            ),
            Badge(
                codigo = "constancia_gentil",
                nome = "Constância Gentil",
                descricao = "Ritmo vale mais que pressa.",
                criterio = "Aprender em 4 semanas diferentes",
                cor = AMBAR,
                icone = "sol",
            ),
            Badge(
                codigo = "jardim_privado",
                nome = "Jardim Privado",
                descricao = "Você escolhe o que floresce e o que fica protegido.",
                criterio = "Concluir a trilha Privacidade e dados",
                cor = LAVANDA,
                icone = "cadeado",
                disponivel = false,
            ),
            Badge(
                codigo = "respiracao",
                nome = "Respiração",
                descricao = "Tela com hora de começar e hora de acabar.",
                criterio = "Concluir a trilha Telas com equilíbrio",
                cor = VERDE,
                icone = "folha",
                disponivel = false,
            ),
            Badge(
                codigo = "mente_serena",
                nome = "Mente Serena",
                descricao = "Cuidar da cabeça também é segurança.",
                criterio = "Concluir a trilha Bem-estar digital",
                cor = LAVANDA,
                icone = "coracao",
                disponivel = false,
            ),
            Badge(
                codigo = "guardiao",
                nome = "Guardião da Família",
                descricao = "Acompanhar com conversa, não com vigilância.",
                criterio = "Concluir a trilha Crianças e adolescentes online",
                cor = VERDE,
                icone = "familia",
                disponivel = false,
            ),
            Badge(
                codigo = "voz_que_ajuda",
                nome = "Voz que Ajuda",
                descricao = "Saber pedir e oferecer ajuda é força.",
                criterio = "Concluir Bem-estar e uma atividade de apoio",
                cor = VERDE,
                icone = "maos",
                disponivel = false,
            ),
            Badge(
                codigo = "colheita_completa",
                nome = "Colheita Completa",
                descricao = "Um jardim inteiro, no seu tempo.",
                criterio = "Concluir 4 trilhas",
                cor = PEONIA,
                icone = "jardim",
                tier = BadgeTier.PERCURSO,
                disponivel = false,
            ),
            Badge(
                codigo = "familia_aprendeu",
                nome = "Família Aprendeu",
                descricao = "Cada um aprendeu algo — e contou em casa.",
                criterio = "3 perfis da família concluírem uma trilha",
                cor = PEONIA,
                icone = "familia",
                tier = BadgeTier.PERCURSO,
                disponivel = false,
            ),
        )

    fun porCodigo(codigo: String): Badge? = todos.firstOrNull { it.codigo == codigo }
}
