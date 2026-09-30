package br.com.vippela

import br.com.vippela.data.Role
import br.com.vippela.data.auth.AuthGateway
import br.com.vippela.data.auth.SessaoLocal
import java.time.Instant

/**
 * Servidor de mentira com o mesmo contrato do de verdade: guarda senha por
 * e-mail, mantém a sessão por idToken do Google e recusa e-mail repetido.
 * Sem rede e sem espera, para os testes de tela não dependerem de backend.
 */
class FakeAuthGateway : AuthGateway {
    private val senhas = linkedMapOf<String, String>()
    private val porEmail = linkedMapOf<String, SessaoLocal>()
    private val porGoogleToken = linkedMapOf<String, SessaoLocal>()

    /** Só devolve sessão em restaurar() quando o teste pedir. */
    var sessaoRestauravel: SessaoLocal? = null

    override val servidorConfigurado = true

    init {
        criar("Cleber", "cleber@example.com", "senha123", Role.RESPONSAVEL)
        criar("Marina", "marina@example.com", "senha123", Role.FAMILIAR)
    }

    private fun criar(
        nome: String,
        email: String,
        senha: String,
        tipo: Role,
        googleToken: String? = null,
    ): SessaoLocal {
        val chave = email.trim().lowercase()
        val sessao =
            SessaoLocal(
                id = "id-$chave",
                nome = nome,
                email = chave,
                tipo = tipo,
                token = "token-$chave",
                expiraEm = Instant.now().plusSeconds(3600),
            )
        senhas[chave] = senha
        porEmail[chave] = sessao
        googleToken?.let { porGoogleToken[it] = sessao }
        return sessao
    }

    override suspend fun cadastrar(
        nome: String,
        email: String,
        senha: String,
        tipo: Role,
    ): SessaoLocal {
        val chave = email.trim().lowercase()
        if (senhas.containsKey(chave)) throw IllegalStateException("Este e-mail já está cadastrado.")
        return criar(nome, chave, senha, tipo)
    }

    override suspend fun entrar(email: String, senha: String): SessaoLocal {
        val chave = email.trim().lowercase()
        if (senhas[chave] != senha) throw IllegalStateException("E-mail ou senha incorretos.")
        return porEmail.getValue(chave)
    }

    override suspend fun entrarComGoogle(idToken: String, nome: String?, tipo: Role): SessaoLocal {
        porGoogleToken[idToken]?.let { return it }
        val email = "google-${idToken.takeLast(6).lowercase()}@example.com"
        return criar(nome ?: "Usuário", email, "", tipo, idToken)
    }

    override suspend fun restaurar(): SessaoLocal? = sessaoRestauravel

    /** Sair encerra a sessão, não apaga a conta. */
    override suspend fun sair() {
        sessaoRestauravel = null
    }
}
