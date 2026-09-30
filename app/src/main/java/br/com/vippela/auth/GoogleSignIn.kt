package br.com.vippela.auth

import android.content.Context
import androidx.credentials.*
import br.com.vippela.BuildConfig
import com.google.android.libraries.identity.googleid.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * `id` é o idToken do Firebase: é ele que o backend valida e troca pela
 * sessão do servidor. O uid fica só como rótulo para a interface.
 */
data class GoogleProfile(val id: String, val name: String, val email: String)

class GoogleSignIn(private val context: Context) {
    /**
     * Só vale com a flag `vippela.google` ligada, o `google-services.json`
     * presente e o Firebase iniciado. Sem isso, o aplicativo continua
     * funcionando só com e-mail e senha.
     */
    fun isConfigured(): Boolean =
        BuildConfig.VIPPELA_GOOGLE_ATIVO &&
            clientId().isNotBlank() &&
            FirebaseApp.getApps(context).isNotEmpty()

    private fun clientId(): String {
        val id =
            context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (id == 0) "" else context.getString(id)
    }

    suspend fun signIn(): GoogleProfile {
        check(isConfigured()) { "Google não configurado" }
        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId()).build())
                .build()
        val response = CredentialManager.create(context).getCredential(context, request)
        val credential = response.credential as? CustomCredential ?: error("Credencial inesperada")
        check(credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
        val google = GoogleIdTokenCredential.createFrom(credential.data)
        // Firebase valida o token antes de abrir a sessão.
        val firebase =
            FirebaseAuth.getInstance()
                .signInWithCredential(GoogleAuthProvider.getCredential(google.idToken, null))
                .await()
        val user = firebase.user ?: error("Conta indisponível")
        // Token fresco para o backend conferir a assinatura.
        val idToken =
            user.getIdToken(true).await().token ?: error("Token do Google indisponível")
        return GoogleProfile(
            idToken,
            user.displayName ?: "Usuário",
            user.email.orEmpty(),
        )
    }

    suspend fun signOut() {
        if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseAuth.getInstance().signOut()
        runCatching {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        }
    }
}
