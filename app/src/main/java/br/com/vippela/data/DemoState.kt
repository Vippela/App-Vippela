package br.com.vippela.data

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

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

class DemoState : ViewModel() {
    var darkMode by mutableStateOf(false)
    var googleUid by mutableStateOf<String?>(null)
    private val contactEmails = mutableStateMapOf<String, String>()
    private val contactPhones = mutableStateMapOf<String, String>()
    val photos = mutableStateMapOf<String, String>()
    val profileKey
        get() = googleUid?.let { "google:$it" } ?: if (isParent) "parent" else "member:$selectedId"

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
    var email by mutableStateOf("responsavel@vippela.demo")
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
    var linked by mutableStateOf(false)
    var notifications by mutableStateOf(true)
    var reminders by mutableStateOf(true)
    val isParent
        get() = role == Role.RESPONSAVEL

    val currentName
        get() = if (isParent) displayName else selected.name

    fun login(address: String, password: String): Boolean {
        if (password != "vippela123") return false
        val authenticatedRole =
            when (address.trim().lowercase()) {
                "responsavel@vippela.demo" -> Role.RESPONSAVEL
                "familiar@vippela.demo" -> Role.FAMILIAR
                else -> return false
            }
        startSession(authenticatedRole, address)
        return true
    }

    fun register(name: String, address: String, newRole: Role) {
        startSession(newRole, address, name)
    }

    fun loginWithGoogle(id: String, name: String, address: String, newRole: Role) {
        startSession(newRole, address, name, id)
    }

    fun logout() {
        role = null
        googleUid = null
        selectedId = 1
    }

    private fun startSession(
        newRole: Role,
        address: String,
        name: String? = null,
        externalId: String? = null,
    ) {
        googleUid = externalId
        role = newRole
        email = address.trim()
        selectedId = 1
        val cleanName = name?.trim()?.takeIf { it.isNotEmpty() } ?: return
        if (newRole == Role.RESPONSAVEL) {
            displayName = cleanName
        } else {
            val memberIndex = members.indexOfFirst { it.id == selectedId }
            if (memberIndex >= 0) members[memberIndex] = members[memberIndex].copy(name = cleanName)
        }
    }

    fun setAllowed(name: String, value: Boolean) {
        apps[selectedId] =
            apps.getValue(selectedId).map { if (it.name == name) it.copy(allowed = value) else it }
    }

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
