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

    suspend fun cadastrar(nome: String, email: String, senha: String, tipo: Role, dataNascimento: String? = null): SessaoLocal

    suspend fun entrar(email: String, senha: String): SessaoLocal

    suspend fun entrarComGoogle(idToken: String, nome: String?, tipo: Role): SessaoLocal

    /** Confere o token guardado com o servidor; null se ele não valer mais. */
    suspend fun restaurar(): SessaoLocal?

    suspend fun sair()

    fun limparSessao() {}
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

    private fun api(base: String): AuthApi {
        require(NetworkModule.validUrl(base)) { "Configure o endereço do servidor." }
        return NetworkModule.auth(base) { sessoes.token(base) }
    }

    override suspend fun cadastrar(
        nome: String,
        email: String,
        senha: String,
        tipo: Role,
        dataNascimento: String?,
    ): SessaoLocal = autenticar { it.cadastrar(CadastroRequest(nome.trim(), email.trim(), senha, tipo.name, dataNascimento)) }

    override suspend fun entrar(email: String, senha: String): SessaoLocal =
        autenticar { it.entrar(LoginRequest(email.trim(), senha)) }

    override suspend fun entrarComGoogle(idToken: String, nome: String?, tipo: Role): SessaoLocal =
        autenticar { it.entrarComGoogle(GoogleRequest(idToken, tipo.name, nome?.trim())) }

    override suspend fun restaurar(): SessaoLocal? {
        val base = servidor().trim()
        val local = sessoes.atual(base) ?: return null
        return try {
            val response = withContext(Dispatchers.IO) { api(base).eu() }
            if (servidor().trim() != base || sessoes.token(base) != local.token) return null
            sessoes.salvar(response.copy(token = local.token), base)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: IOException) {
            // Falha de rede não invalida uma sessão local ainda válida.
            if (servidor().trim() == base && sessoes.token(base) == local.token) local else null
        } catch (e: HttpException) {
            if (e.code() in listOf(401, 403) && sessoes.token(base) == local.token) sessoes.limpar()
            if (e.code() >= 500 && servidor().trim() == base) sessoes.atual(base) else null
        }
    }

    override suspend fun sair() {
        val base = servidor().trim()
        val token = sessoes.token(base)
        sessoes.limpar()
        if (token == null) return
        runCatching {
            withContext(Dispatchers.IO) { NetworkModule.auth(base) { token }.sair() }
        }
    }

    override fun limparSessao() = sessoes.limpar()

    private suspend fun autenticar(chamada: suspend (AuthApi) -> SessaoResponse): SessaoLocal {
        val base = servidor().trim()
        val resposta = chamada(api(base))
        check(servidor().trim() == base) { "O servidor foi alterado. Entre novamente." }
        return sessoes.salvar(resposta, base) ?: error("O servidor não devolveu uma sessão.")
    }

    companion object {
        /** Mensagem em português para o que deu errado, sem vazar nada do servidor. */
        fun mensagemDeErro(erro: Exception): String =
            when {
                erro is HttpException ->
                    when (erro.code()) {
                        400 -> mensagemDeValidacao(erro)
                        401 -> "E-mail ou senha incorretos."
                        403 -> "Este acesso não é permitido."
                        404 -> "Cadastro/login não encontrado neste servidor."
                        409 -> "Este e-mail já está cadastrado."
                        429 -> "Muitas tentativas. Aguarde alguns minutos."
                        503 -> if (runCatching { corpoErro(erro)?.get("code")?.asString }.getOrNull() == "DATABASE_UNAVAILABLE")
                            "Cadastro e login estão indisponíveis no servidor. Tente novamente mais tarde."
                            else "Este recurso não está disponível no servidor agora."
                        else -> "Não foi possível concluir. Tente de novo."
                    }
                erro is IOException ->
                    "Não foi possível falar com o servidor. Confira o endereço e a conexão."
                else -> erro.message?.takeIf { it.isNotBlank() }
                    ?: "Não foi possível concluir. Tente de novo."
            }

        private fun corpoErro(erro: HttpException): com.google.gson.JsonObject? = runCatching {
            val body = erro.response()?.errorBody()?.string() ?: return@runCatching null
            if (body.length > 16000) return@runCatching null
            com.google.gson.JsonParser.parseString(body).asJsonObject
        }.getOrNull()

        private fun mensagemDeValidacao(erro: HttpException): String {
            val fields = corpoErro(erro) ?: return "Confira os dados informados."
            return when {
                fields.has("dataNascimento") -> "Confira a data de nascimento. Use uma data válida, até hoje."
                fields.has("nome") -> "O nome deve ter entre 2 e 50 caracteres."
                fields.has("email") -> "Informe um e-mail válido, com até 254 caracteres."
                fields.has("senha") -> "Use pelo menos 8 caracteres e até 72 bytes na senha."
                fields.has("tipoConta") -> "Escolha Responsável ou Familiar."
                else -> "Confira os dados informados."
            }
        }
    }
}
