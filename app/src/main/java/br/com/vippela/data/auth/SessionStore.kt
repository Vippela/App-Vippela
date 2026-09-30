package br.com.vippela.data.auth

import android.content.Context
import br.com.vippela.data.Role
import br.com.vippela.data.auth.model.SessaoResponse
import java.time.Instant

data class SessaoLocal(
    val id: String,
    val nome: String,
    val email: String,
    val tipo: Role,
    val token: String,
    val expiraEm: Instant,
) {
    fun expirada(agora: Instant = Instant.now()) = !expiraEm.isAfter(agora)
}

/**
 * Sessão do aparelho: só o token e o perfil, nada de senha. Como as
 * chaves de vínculo, fica em SharedPreferences — num aparelho sem root
 * isso é leitura local; para blindar de vez, dá para migrar para
 * EncryptedSharedPreferences depois.
 */
class SessionStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("sessao", Context.MODE_PRIVATE)

    fun salvar(resposta: SessaoResponse): SessaoLocal? {
        val token = resposta.token ?: return null
        val expiraEm =
            resposta.expiraEm?.let { runCatching { Instant.parse(it) }.getOrNull() }
                ?: Instant.now().plusSeconds(7 * 24 * 3600)
        val sessao =
            SessaoLocal(
                id = resposta.id,
                nome = resposta.nome,
                email = resposta.email,
                tipo = runCatching { Role.valueOf(resposta.tipoConta) }.getOrDefault(Role.RESPONSAVEL),
                token = token,
                expiraEm = expiraEm,
            )
        prefs.edit()
            .putString("id", sessao.id)
            .putString("nome", sessao.nome)
            .putString("email", sessao.email)
            .putString("tipo", sessao.tipo.name)
            .putString("token", token)
            .putString("expira", expiraEm.toString())
            .apply()
        return sessao
    }

    /** Sessão guardada, ou null se não houver ou o token já tiver passado. */
    fun atual(): SessaoLocal? {
        val token = prefs.getString("token", null) ?: return null
        val expiraEm = prefs.getString("expira", null)?.let { runCatching { Instant.parse(it) }.getOrNull() }
        val sessao =
            SessaoLocal(
                id = prefs.getString("id", "").orEmpty(),
                nome = prefs.getString("nome", "").orEmpty(),
                email = prefs.getString("email", "").orEmpty(),
                tipo =
                    runCatching { Role.valueOf(prefs.getString("tipo", null).orEmpty()) }
                        .getOrDefault(Role.RESPONSAVEL),
                token = token,
                expiraEm = expiraEm ?: Instant.EPOCH,
            )
        return if (sessao.expirada()) {
            limpar()
            null
        } else sessao
    }

    fun token(): String? = atual()?.token

    fun limpar() {
        prefs.edit().clear().apply()
    }
}
