package br.com.vippela.data

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.vippela.data.auth.AuthGateway
import br.com.vippela.data.auth.AuthRepository
import br.com.vippela.data.auth.SessaoLocal
import br.com.vippela.data.auth.SessionStore
import br.com.vippela.data.linking.*
import br.com.vippela.data.linking.model.*
import kotlinx.coroutines.*

enum class Role {
    RESPONSAVEL,
    FAMILIAR,
}

data class FamilyMember(
    val id: Int,
    val name: String,
    val age: Int,
    val minutes: Int,
    val risk: String,
)

data class AppEntry(val name: String, val minutes: Int, val allowed: Boolean)

data class Request(
    val id: Int,
    val memberId: Int,
    val app: String,
    val message: String,
    val status: String = "Aguardando",
)

data class Goal(val title: String, val minutes: Int)

data class Lesson(
    val title: String,
    val text: String,
    val question: String,
    val answers: List<String>,
    val correct: Int,
)

val lessons =
    listOf(
        Lesson(
            "O que esperar do curso?",
            "Vamos aprender a reconhecer mensagens suspeitas e cuidar da nossa privacidade. Antes de abrir um link, observe quem enviou e converse com alguém de confiança se tiver dúvidas.",
            "O que fazer ao receber um link desconhecido?",
            listOf(
                "Abrir imediatamente",
                "Conferir a origem antes de abrir",
                "Enviar para todos os amigos",
            ),
            1,
        ),
        Lesson(
            "Tipos de ataques de phishing",
            "Phishing é uma tentativa de enganar você para obter informações. Mensagens com urgência, prêmios inesperados e pedidos de senha merecem atenção. Acesse o serviço pelo aplicativo ou endereço que você já conhece.",
            "Uma mensagem pede sua senha para liberar um prêmio. Você deve:",
            listOf(
                "Informar a senha",
                "Não informar e verificar pelo canal oficial",
                "Mandar uma foto do documento",
            ),
            1,
        ),
        Lesson(
            "Agentes maliciosos",
            "Pessoas mal-intencionadas podem fingir ser amigos ou empresas. Não compartilhe códigos de acesso e procure ajuda se alguém pressionar você. Pedir ajuda é uma forma de se proteger.",
            "Alguém pede seu código de verificação. Qual é a atitude segura?",
            listOf("Compartilhar o código", "Publicar o código", "Guardar o código e buscar ajuda"),
            2,
        ),
        Lesson(
            "Mensagens urgentes",
            "Golpistas usam urgência para impedir que você pense. Pare, confira o remetente e procure o canal oficial.",
            "Uma mensagem diz que sua conta será apagada em cinco minutos. O que fazer?",
            listOf("Clicar sem pensar", "Parar e verificar pelo canal oficial", "Enviar sua senha"),
            1,
        ),
        Lesson(
            "Confira o endereço",
            "Um endereço pode imitar o nome de um serviço. Abra o aplicativo que você já usa quando precisar conferir um aviso.",
            "Como conferir uma cobrança inesperada?",
            listOf("Pelo link recebido", "Pelo aplicativo oficial", "Enviando o cartão no chat"),
            1,
        ),
        Lesson(
            "Peça ajuda",
            "Se você clicou em algo suspeito, avise uma pessoa de confiança. Pedir ajuda cedo facilita lidar com o problema.",
            "Você abriu um link suspeito. Qual o próximo passo?",
            listOf("Esconder o ocorrido", "Compartilhar o link", "Avisar uma pessoa de confiança"),
            2,
        ),
        Lesson(
            "Senhas únicas",
            "Use senhas diferentes para cada conta e não as compartilhe. Um gerenciador de senhas pode ajudar sua família.",
            "Qual hábito protege suas contas?",
            listOf("Repetir a mesma senha", "Usar senhas diferentes", "Publicar a senha"),
            1,
        ),
        Lesson(
            "Códigos de acesso",
            "Códigos de verificação são pessoais. Não os entregue a alguém que entrou em contato com você.",
            "Uma pessoa pede um código que chegou por SMS. Você deve:",
            listOf("Enviar imediatamente", "Manter o código privado", "Enviar metade do código"),
            1,
        ),
        Lesson(
            "Aprender em família",
            "Conversem sobre dúvidas e experiências. A segurança digital é construída com orientação e cuidado.",
            "Como ajudar alguém que encontrou uma mensagem suspeita?",
            listOf("Ouvir e verificar juntos", "Culpar a pessoa", "Ignorar a dúvida"),
            0,
        ),
    )

class DemoState(private val auth: AuthGateway? = null) : ViewModel() {
    var darkMode by mutableStateOf(false)

    /** Id da conta no servidor; separa o vínculo de cada pessoa. */
    var usuarioId by mutableStateOf<String?>(null)
        private set

    var entrouComGoogle by mutableStateOf(false)
        private set

    /** Muda a cada login/logout para a tela recarregar o escopo de vínculo. */
    var sessaoGeracao by mutableIntStateOf(0)
        private set

    private val contactEmails = mutableStateMapOf<String, String>()
    private val contactPhones = mutableStateMapOf<String, String>()
    val photos = mutableStateMapOf<String, String>()
    val profileKey
        get() = usuarioId?.let { "user:$it" } ?: if (isParent) "parent" else "member:$selectedId"

    var contactEmail: String
        get() = contactEmails[profileKey] ?: email
        set(value) {
            contactEmails[profileKey] = value
        }

    var phone: String
        get() = contactPhones[profileKey] ?: ""
        set(value) {
            contactPhones[profileKey] = value
        }

    var photo: String?
        get() = photos[profileKey]
        set(value) {
            if (value == null) photos.remove(profileKey) else photos[profileKey] = value
        }

    var role by mutableStateOf<Role?>(null)
    var displayName by mutableStateOf("Cleber")
    var email by mutableStateOf("")
    var selectedId by mutableIntStateOf(1)
    val members =
        mutableStateListOf(
            FamilyMember(1, "Marina Garcia", 17, 134, "Atenção"),
            FamilyMember(2, "Gabriel Silva", 12, 95, "Baixo"),
            FamilyMember(3, "Maria Liz", 10, 186, "Alto"),
        )
    val selected
        get() = members.first { it.id == selectedId }

    val limits = mutableStateMapOf(1 to 180, 2 to 180, 3 to 180)
    val apps =
        mutableStateMapOf<Int, List<AppEntry>>().apply {
            members.forEach { member ->
                put(
                    member.id,
                    listOf(
                        AppEntry("YouTube", 72, false),
                        AppEntry("Instagram", 45, true),
                        AppEntry("TikTok", 17, false),
                        AppEntry("Khan Academy", 25, true),
                    ),
                )
            }
        }
    val requests = mutableStateListOf(Request(1, 1, "YouTube", "Para estudar"))
    val goals =
        mutableStateMapOf<Int, List<Goal>>().apply {
            members.forEach { put(it.id, listOf(Goal("Estudar", 120), Goal("Dormir", 480))) }
        }
    val completed = mutableStateMapOf<String, Boolean>()
    val linked
        get() = currentLink?.status == "active"

    var remoteLinks by mutableStateOf<List<DeviceLinkResponse>>(emptyList())
        private set

    var childLink by mutableStateOf<DeviceLinkResponse?>(null)
        private set

    val currentLink
        get() =
            if (isParent) remoteLinks.firstOrNull { it.memberKey == selectedId.toString() }
            else childLink

    var linkCode by mutableStateOf<LinkCodeResponse?>(null)
        private set

    var linkError by mutableStateOf<String?>(null)
        private set

    var linkBusy by mutableStateOf(false)
        private set

    var serverAddress by mutableStateOf("")
        private set

    private var linkStore: LinkStore? = null
    private var linkRepository: DeviceLinkRepository? = null
    private var remoteJob: Job? = null
    private var remoteAction: Job? = null
    private var sessionGeneration = 0

    fun attachLinks(store: LinkStore) {
        sessionGeneration++
        remoteJob?.cancel()
        remoteAction?.cancel()
        linkStore = store
        serverAddress = store.server
        remoteLinks = emptyList()
        childLink = null
        linkCode = null
        linkError = null
        linkBusy = false
        linkRepository = null
        if (role == null) return
        val account = usuarioId ?: "email:${email.trim().lowercase()}"
        val scope = store.scope(account + ":" + role!!.name)
        store.activate(if (isParent) null else scope)
        if (!store.configured) return
        val repository = DeviceLinkRepository(store, scope)
        linkRepository = repository
        if (!isParent) childLink = store.cached(scope) else linkCode = store.pending(scope)
        val parent = isParent
        remoteJob =
            viewModelScope.launch {
                while (isActive) {
                    try {
                        if (parent) {
                            val received = repository.list()
                            remoteLinks =
                                received.map { incoming ->
                                    remoteLinks.firstOrNull {
                                        it.id == incoming.id && it.revision > incoming.revision
                                    } ?: incoming
                                }
                            remoteLinks.forEach { link ->
                                val id = link.memberKey.toIntOrNull() ?: return@forEach
                                val index = members.indexOfFirst { it.id == id }
                                if (index >= 0)
                                    members[index] = members[index].copy(name = link.memberName)
                                else {
                                    members.add(FamilyMember(id, link.memberName, 0, 0, "Baixo"))
                                    limits[id] = 180
                                    apps[id] = emptyList()
                                    goals[id] = emptyList()
                                }
                            }
                            val pending = linkCode
                            if (
                                pending != null && repository.status(pending.id).status == "active"
                            ) {
                                store.savePending(scope, null)
                                linkCode = null
                            }
                        } else {
                            childLink = repository.sync() ?: childLink
                        }
                        linkError = null
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        linkError = remoteMessage(e)
                    }
                    delay(5000)
                }
            }
    }

    fun configureServer(value: String): Boolean {
        if (!NetworkModule.validUrl(value.trim())) {
            linkError = "Informe uma URL válida. Use HTTPS fora da versão de testes."
            return false
        }
        val store = linkStore ?: return false
        store.server = value
        attachLinks(store)
        return true
    }

    fun generateLinkCode() {
        val member = selected
        val owner = displayName
        remoteOperation { repository ->
            linkCode = repository.generate(member.id.toString(), member.name, owner)
        }
    }

    fun confirmLinkCode(code: String) {
        remoteOperation { repository ->
            childLink = repository.confirm(code)
            childLink = repository.sync() ?: childLink
        }
    }

    fun changeRemoteRule(packageName: String, blocked: Boolean) {
        val id = currentLink?.id ?: return
        if (!isParent) return
        remoteOperation { repository ->
            val updated = repository.rule(id, packageName, blocked)
            remoteLinks = remoteLinks.map { if (it.id == id) updated else it }
        }
    }

    private fun remoteOperation(action: suspend (DeviceLinkRepository) -> Unit) {
        if (linkBusy) return
        val repository =
            linkRepository
                ?: run {
                    linkError = "Configure o servidor do vínculo nos dois celulares."
                    return
                }
        linkBusy = true
        linkError = null
        val generation = sessionGeneration
        remoteAction =
            viewModelScope.launch {
                try {
                    action(repository)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (generation == sessionGeneration) linkError = remoteMessage(e)
                } finally {
                    if (generation == sessionGeneration) linkBusy = false
                }
            }
    }

    private fun remoteMessage(error: Exception): String =
        when ((error as? retrofit2.HttpException)?.code()) {
            400 -> "Confira o código e os dados enviados."
            401,
            403 -> "Este acesso não pertence ao vínculo selecionado."
            404 -> "Vínculo não encontrado neste servidor."
            409 -> "Este familiar ou aparelho já possui um vínculo."
            410 -> "O código expirou. Peça um novo ao responsável."
            429 -> "Muitas tentativas. Aguarde um minuto."
            else -> "Não foi possível sincronizar. Confira o servidor e a conexão."
        }

    var notifications by mutableStateOf(true)
    var reminders by mutableStateOf(true)
    val isParent
        get() = role == Role.RESPONSAVEL

    val currentName
        get() = if (isParent) displayName else selected.name

    var registrationEmail by mutableStateOf("")
    var pendingGoogle by mutableStateOf<br.com.vippela.auth.GoogleProfile?>(null)
        private set

    /** Mensagem do último login/cadastro que deu errado, para a tela mostrar. */
    var authError by mutableStateOf<String?>(null)
        private set

    var authBusy by mutableStateOf(false)
        private set

    val servidorPronto
        get() = auth?.servidorConfigurado == true

    private val authScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private fun autenticar(
        chamada: suspend () -> SessaoLocal,
        concluido: (Boolean) -> Unit,
    ) {
        val gateway = auth
        if (gateway == null) {
            authError = "Este aplicativo ainda não foi conectado a um servidor."
            concluido(false)
            return
        }
        if (authBusy) return
        authBusy = true
        authError = null
        authScope.launch {
            try {
                aplicarSessao(chamada())
                concluido(true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                authError = AuthRepository.mensagemDeErro(e)
                concluido(false)
            } finally {
                authBusy = false
            }
        }
    }

    fun login(address: String, password: String, concluido: (Boolean) -> Unit = {}) {
        autenticar({ requireNotNull(auth).entrar(address, password) }, concluido)
    }

    fun register(
        name: String,
        address: String,
        password: String,
        newRole: Role,
        concluido: (Boolean) -> Unit = {},
    ) {
        autenticar(
            { requireNotNull(auth).cadastrar(name, address, password, newRole) },
            {
                if (it) registrationEmail = ""
                concluido(it)
            },
        )
    }

    /** Guarda o idToken e pergunta o tipo de conta; o servidor cria ou reutiliza. */
    fun loginWithGoogle(idToken: String, name: String, address: String): Boolean {
        pendingGoogle = br.com.vippela.auth.GoogleProfile(idToken, name, address)
        return false
    }

    fun completeGoogleRegistration(newRole: Role, concluido: (Boolean) -> Unit = {}) {
        val google = pendingGoogle ?: return
        autenticar({ requireNotNull(auth).entrarComGoogle(google.id, google.name, newRole) }) {
            if (it) pendingGoogle = null
            concluido(it)
        }
    }

    fun cancelGoogleRegistration() {
        pendingGoogle = null
    }

    /** Confere o token guardado com o servidor ao abrir o app. */
    fun restaurarSessao() {
        val gateway = auth ?: return
        if (!gateway.servidorConfigurado) return
        authScope.launch {
            gateway.restaurar()?.let { sessao -> aplicarSessao(sessao) }
        }
    }

    fun logout() {
        sessionGeneration++
        remoteJob?.cancel()
        remoteAction?.cancel()
        linkStore?.activate(null)
        linkRepository = null
        remoteLinks = emptyList()
        childLink = null
        linkCode = null
        linkBusy = false
        pendingGoogle = null
        role = null
        usuarioId = null
        entrouComGoogle = false
        authError = null
        selectedId = 1
        sessaoGeracao++
        auth?.let { gateway -> authScope.launch { gateway.sair() } }
    }

    private fun aplicarSessao(sessao: SessaoLocal) {
        usuarioId = sessao.id
        entrouComGoogle = sessao.email.isGoogleLike()
        role = sessao.tipo
        email = sessao.email
        selectedId = 1
        sessaoGeracao++
        val cleanName = sessao.nome.trim()
        if (cleanName.isEmpty()) return
        if (sessao.tipo == Role.RESPONSAVEL) {
            displayName = cleanName
        } else {
            val memberIndex = members.indexOfFirst { it.id == selectedId }
            if (memberIndex >= 0) members[memberIndex] = members[memberIndex].copy(name = cleanName)
        }
    }

    private fun String.isGoogleLike() = contains("@") && endsWith("gmail.com", ignoreCase = true)

    fun requestApp(name: String, message: String) {
        if (
            requests.none {
                it.memberId == selectedId && it.app == name && it.status == "Aguardando"
            }
        ) {
            requests.add(
                Request((requests.maxOfOrNull { it.id } ?: 0) + 1, selectedId, name, message)
            )
        }
    }

    fun decide(id: Int, approved: Boolean) {
        val index = requests.indexOfFirst { it.id == id }
        if (index < 0) return
        val req = requests[index]
        requests[index] = req.copy(status = if (approved) "Liberado" else "Não liberado")
        if (approved)
            apps[req.memberId] =
                apps.getValue(req.memberId).map {
                    if (it.name == req.app) it.copy(allowed = true) else it
                }
    }

    fun addMember(name: String, age: Int) {
        val id = (members.maxOfOrNull { it.id } ?: 0) + 1
        members.add(FamilyMember(id, name, age, 0, "Baixo"))
        limits[id] = 180
        apps[id] = apps.getValue(1).map { it.copy(minutes = 0) }
        goals[id] = emptyList()
        selectedId = id
    }

    fun lessonKey(index: Int) = "${role?.name}:${if (isParent) 0 else selectedId}:$index"
}
