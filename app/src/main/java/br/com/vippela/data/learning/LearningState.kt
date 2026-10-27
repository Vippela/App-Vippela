package br.com.vippela.data.learning

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import java.time.LocalDate
import java.time.temporal.IsoFields
import java.util.Locale

/**
 * Estado da trilha: conteúdo empacotado no app + progresso local por perfil.
 *
 * O progresso é gravado em SharedPreferences (JSON) para funcionar offline.
 * A sincronização com o backend entra na fase de integração; aqui a chave
 * `progresso:<perfil>` já separa cada pessoa da família.
 */
class LearningState(private val context: Context) {
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("vippela_learning", Context.MODE_PRIVATE)
    private val arquivos =
        listOf("learning/trilha_contas.json", "learning/trilha_golpes.json")

    val trilhas: List<Trilha> = carregarConteudo()

    var escopo by mutableStateOf("")
        private set

    var ultimosSelos by mutableStateOf<List<Badge>>(emptyList())
        private set

    var ultimoResultado by mutableStateOf<ResultadoQuiz?>(null)
        private set

    private var progresso by mutableStateOf(LearningProgress())

    // ---------------------------------------------------------------- sessão

    fun bind(novoEscopo: String) {
        if (novoEscopo == escopo) return
        escopo = novoEscopo
        progresso = lerProgresso(novoEscopo)
        ultimosSelos = emptyList()
        ultimoResultado = null
    }

    // ---------------------------------------------------------------- consultas

    fun trilha(slug: String): Trilha? = trilhas.firstOrNull { it.slug == slug }

    fun licao(id: String): Licao? =
        trilhas.asSequence().flatMap { it.listaLicoes }.firstOrNull { it.id == id }

    fun moduloDe(licaoId: String): Pair<Trilha, Modulo>? =
        trilhas.firstNotNullOfOrNull { trilha ->
            trilha.listaModulos.firstOrNull { modulo ->
                modulo.listaLicoes.any { it.id == licaoId }
            }?.let { trilha to it }
        }

    fun licaoConcluida(id: String): Boolean = progresso.licoes.containsKey(id)

    fun acertosLicao(id: String): Int = progresso.notas[id] ?: 0

    fun licoesConcluidas(trilha: Trilha): Int =
        trilha.listaLicoes.count { licaoConcluida(it.id) }

    fun progresso(trilha: Trilha): Float {
        val total = trilha.listaLicoes.size
        return if (total == 0) 0f else licoesConcluidas(trilha).toFloat() / total
    }

    fun acertosModulo(modulo: Modulo): Int = progresso.modulos[modulo.id] ?: 0

    /** Módulo liberado quando o anterior foi aprovado. O primeiro é sempre aberto. */
    fun moduloLiberado(trilha: Trilha, indice: Int): Boolean {
        if (indice <= 0) return true
        val anterior = trilha.listaModulos.getOrNull(indice - 1) ?: return true
        return moduloAprovado(anterior)
    }

    fun moduloAprovado(modulo: Modulo): Boolean {
        val total = modulo.listaLicoes.size
        if (total == 0) return false
        val licoesFeitas = modulo.listaLicoes.all { licaoConcluida(it.id) }
        return licoesFeitas && acertosModulo(modulo) * 100 / total >= 66
    }

    fun trilhaConcluida(trilha: Trilha): Boolean =
        trilha.listaModulos.isNotEmpty() && trilha.listaModulos.all { moduloAprovado(it) }

    fun trilhasConcluidas(): Int = trilhas.count { trilhaConcluida(it) }

    fun proximaLicao(trilha: Trilha): Licao? =
        trilha.listaLicoes.firstOrNull { !licaoConcluida(it.id) } ?: trilha.listaLicoes.lastOrNull()

    fun continuar(): Pair<Trilha, Licao>? {
        val daVez =
            trilhas.firstOrNull { t ->
                progresso.licoes.isNotEmpty() &&
                    licoesConcluidas(t) in 1 until t.listaLicoes.size
            }
        if (daVez != null) proximaLicao(daVez)?.let { return daVez to it }
        return trilhas.firstOrNull()?.let { t -> proximaLicao(t)?.let { t to it } }
    }

    fun seloConquistado(codigo: String): Boolean = progresso.selos.containsKey(codigo)

    fun dataSelo(codigo: String): String? = progresso.selos[codigo]

    fun certificadoExistente(slug: String): String? = progresso.certificados[slug]

    fun atividadesFamilia(): Int = progresso.atividadesFamilia

    fun desafioDaSemanaFeito(): Boolean = progresso.desafios.contains(semanaAtual())

    fun marcarDesafioDaSemana(): List<Badge> {
        if (!desafioDaSemanaFeito()) {
            progresso.desafios += semanaAtual()
            progresso.atividadesFamilia += 1
            progresso.semanas += semanaAtual()
            commit()
        }
        return avaliarSelos()
    }

    // ---------------------------------------------------------------- ações

    /** Marca a lição quando aprovada, guarda a melhor nota e devolve os selos novos. */
    fun concluirLicao(licao: Licao, acertos: Int, total: Int, aprovado: Boolean): List<Badge> {
        val primeiraVez = !licaoConcluida(licao.id)
        if (aprovado && primeiraVez) {
            progresso.licoes[licao.id] = agora()
            progresso.semanas += semanaAtual()
            if (licao.tipo.equals("FAM", ignoreCase = true)) {
                progresso.atividadesFamilia += 1
            }
        }
        progresso.notas[licao.id] = maxOf(acertosLicao(licao.id), acertos)
        progresso.ultimaLicao = licao.id
        commit()
        return avaliarSelos()
    }

    /** Guarda a melhor checagem de módulo e avalia a trilha. */
    fun registrarModulo(modulo: Modulo, acertos: Int): List<Badge> {
        progresso.modulos[modulo.id] = maxOf(acertosModulo(modulo), acertos)
        commit()
        return avaliarSelos()
    }

    fun emitirCertificado(trilha: Trilha): String {
        progresso.certificados[trilha.slug]?.let { return it }
        val codigo = gerarCodigo()
        progresso.certificados[trilha.slug] = codigo
        commit()
        return codigo
    }

    fun publicarResultado(resultado: ResultadoQuiz) {
        ultimoResultado = resultado
        ultimosSelos = resultado.selos
    }

    fun limparUltimosSelos() {
        ultimosSelos = emptyList()
    }

    /** Conta quantas lições do tipo "em família" já foram feitas. */
    fun licoesFamiliaConcluidas(): Int =
        trilhas.asSequence().flatMap { it.listaLicoes }
            .filter { it.tipo.equals("FAM", ignoreCase = true) }
            .count { licaoConcluida(it.id) }

    // ---------------------------------------------------------------- selos

    private fun avaliarSelos(): List<Badge> {
        val novos = mutableListOf<Badge>()

        fun dar(codigo: String) {
            if (progresso.selos.containsKey(codigo)) return
            val selo = BadgeCatalog.porCodigo(codigo) ?: return
            progresso.selos[codigo] = agora()
            novos += selo
        }

        if (progresso.licoes.isNotEmpty()) dar("primeira_petala")
        if (trilhaConcluidaPorSlug("contas-e-senhas")) dar("raiz_forte")
        if (trilhaConcluidaPorSlug("golpes-e-fraudes")) dar("olho_atento")
        if (licoesFamiliaConcluidas() >= 2) dar("ponte_confianca")
        if (progresso.semanas.size >= 4) dar("constancia_gentil")
        if (trilhasConcluidas() >= 4) dar("colheita_completa")

        if (novos.isNotEmpty()) commit()
        ultimosSelos = novos
        return novos
    }

    private fun trilhaConcluidaPorSlug(slug: String): Boolean =
        trilha(slug)?.let { trilhaConcluida(it) } == true

    // ---------------------------------------------------------------- persistência

    private fun commit() {
        progresso = copiar(progresso)
        if (escopo.isBlank()) return
        prefs.edit().putString("progresso:$escopo", gson.toJson(progresso)).apply()
    }

    private fun copiar(p: LearningProgress) =
        LearningProgress(
            licoes = LinkedHashMap(p.licoes),
            notas = LinkedHashMap(p.notas),
            modulos = LinkedHashMap(p.modulos),
            selos = LinkedHashMap(p.selos),
            certificados = LinkedHashMap(p.certificados),
            semanas = LinkedHashSet(p.semanas),
            desafios = LinkedHashSet(p.desafios),
            ultimaLicao = p.ultimaLicao,
            atividadesFamilia = p.atividadesFamilia,
        )

    private fun lerProgresso(chave: String): LearningProgress {
        val json = prefs.getString("progresso:$chave", null) ?: return LearningProgress()
        return runCatching { gson.fromJson(json, LearningProgress::class.java) }
            .getOrNull() ?: LearningProgress()
    }

    private fun carregarConteudo(): List<Trilha> =
        arquivos.flatMap { nome ->
            runCatching {
                    context.assets.open(nome).bufferedReader().use { it.readText() }
                }
                .mapCatching { gson.fromJson(it, LearningPack::class.java) }
                .getOrNull()
                ?.trilhas
                .orEmpty()
        }

    private fun agora(): String = java.time.Instant.now().toString()

    private fun semanaAtual(): String {
        val hoje = LocalDate.now()
        val ano = hoje.get(IsoFields.WEEK_BASED_YEAR)
        val semana = hoje.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        return String.format(Locale.ROOT, "%d-W%02d", ano, semana)
    }

    private fun gerarCodigo(): String {
        val alfabeto = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        fun bloco() = (1..4).map { alfabeto.random() }.joinToString("")
        return "VP-${bloco()}-${bloco()}"
    }
}
