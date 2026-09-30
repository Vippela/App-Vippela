package br.com.vippela.data.auth

import br.com.vippela.data.Role
import br.com.vippela.data.auth.model.*
import br.com.vippela.data.linking.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/** O que a tela de login precisa saber; o teste injeta uma versão falsa. */
interface AuthGateway {
    val servidorConfigurado: Boolean

    suspend fun cadastrar(nome: String, email: String, senha: String, tipo: Role): SessaoLocal

    suspend fun entrar(email: String, senha: String): SessaoLocal

    suspend fun entrarComGoogle(idToken: String, nome: String?, tipo: Role): SessaoLocal

    /** Confere o token guardado com o servidor; null se ele não valer mais. */
    suspend fun restaurar(): SessaoLocal?

    suspend fun sair()
}

/**
 * Cadastro e login contra o backend. O endereço do servidor é o mesmo
 * configurado no vínculo (LinkStore), porque é a mesma instalação.
 */
class AuthRepository(
    private val servidor: () -> String,
    private val sessoes: SessionStore,
) : AuthGateway {
    override val servidorConfigurado
        get() = NetworkModule.validUrl(servidor().trim())

    private fun api(): AuthApi {
        val base = servidor().trim()
        require(NetworkModule.validUrl(base)) { "Configure o endereço do servidor." }
        return NetworkModule.auth(base) { sessoes.token() }
    }

    override suspend fun cadastrar(
        nome: String,
        email: String,
        senha: String,
        tipo: Role,
    ): SessaoLocal = gravar(api().cadastrar(CadastroRequest(nome.trim(), email.trim(), senha, tipo.name)))

    override suspend fun entrar(email: String, senha: String): SessaoLocal =
        gravar(api().entrar(LoginRequest(email.trim(), senha)))

    override suspend fun entrarComGoogle(idToken: String, nome: String?, tipo: Role): SessaoLocal =
        gravar(api().entrarComGoogle(GoogleRequest(idToken, tipo.name, nome?.trim())))

    override suspend fun restaurar(): SessaoLocal? {
        if (sessoes.atual() == null) return null
        return try {
            withContext(Dispatchers.IO) { api().eu() }.let { sessoes.salvar(it.copy(token = sessoes.token())) }
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 401) sessoes.limpar()
            null
        }
    }

    override suspend fun sair() {
        val token = sessoes.token()
        sessoes.limpar()
        if (token == null) return
        runCatching { withContext(Dispatchers.IO) { api().sair() } }
    }

    private fun gravar(resposta: SessaoResponse): SessaoLocal =
        sessoes.salvar(resposta) ?: throw IllegalStateException("O servidor não devolveu uma sessão.")

    companion object {
        /** Mensagem em português para o que deu errado, sem vazar nada do servidor. */
        fun mensagemDeErro(erro: Exception): String =
            when {
                erro is HttpException ->
                    when (erro.code()) {
                        400 -> "Confira os dados informados."
                        401 -> "E-mail ou senha incorretos."
                        403 -> "Este acesso não é permitido."
                        404 -> "Cadastro/login não encontrado neste servidor."
                        409 -> "Este e-mail já está cadastrado."
                        429 -> "Muitas tentativas. Aguarde alguns minutos."
                        503 -> "Este recurso não está disponível no servidor agora."
                        else -> "Não foi possível concluir. Tente de novo."
                    }
                erro is IOException ->
                    "Não foi possível falar com o servidor. Confira o endereço e a conexão."
                else -> erro.message?.takeIf { it.isNotBlank() }
                    ?: "Não foi possível concluir. Tente de novo."
            }
    }
}
