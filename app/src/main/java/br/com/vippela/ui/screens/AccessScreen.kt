package br.com.vippela.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.com.vippela.auth.GoogleSignIn
import br.com.vippela.data.*
import br.com.vippela.ui.components.*
import br.com.vippela.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AccessScreen(state: DemoState, register: () -> Unit, login: () -> Unit, enter: () -> Unit) {
    val context = LocalContext.current
    val google = remember(context) { GoogleSignIn(context) }
    val googleAtivo = remember(google) { google.isConfigured() }
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    Page {
        Spacer(Modifier.height(32.dp))
        Brand()
        Text(
            "Entrar na Vippela",
            Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(24.dp))
        ServerConfiguration(state)
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Continuar com email", onClick = login)
        // Google só aparece quando a flag vippela.google está ligada e o
        // google-services.json existe; sem isso o acesso é só por e-mail.
        if (googleAtivo) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f))
                Text("  ou  ", color = Muted)
                HorizontalDivider(Modifier.weight(1f))
            }
            Button(
                {
                    scope.launch {
                        busy = true
                        try {
                            val profile = google.signIn()
                            // O backend recebe o idToken; ele decide se a conta
                            // é nova (pede o tipo) ou já existente.
                            if (state.loginWithGoogle(profile.id, profile.name, profile.email))
                                enter()
                            else register()
                        } catch (
                            _: androidx.credentials.exceptions.GetCredentialCancellationException) {
                            message = "Entrada cancelada."
                        } catch (_: androidx.credentials.exceptions.NoCredentialException) {
                            message = "Nenhuma conta Google está disponível neste dispositivo."
                        } catch (cancelled: kotlinx.coroutines.CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            message =
                                "Não foi possível entrar com Google. Confira a conexão e tente novamente."
                        } finally {
                            busy = false
                        }
                    }
                },
                Modifier.fillMaxWidth(),
                enabled = !busy,
                shape = RoundedCornerShape(8.dp),
            ) {
                Image(
                    androidx.compose.ui.res.painterResource(
                        br.com.vippela.R.drawable.ic_google_logo
                    ),
                    null,
                    Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(if (busy) "Entrando…" else "Entrar com Google")
            }
        } else {
            Text(
                "O acesso por Google ainda não está disponível. Use seu e-mail e senha.",
                Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }
        Row(
            Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Não é cliente?", style = MaterialTheme.typography.bodySmall)
            TextButton({
                state.cancelGoogleRegistration()
                register()
            }) {
                Text("Cadastro")
            }
        }
        message?.let { Text(it, color = Muted, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
fun RegistrationChoiceScreen(register: (Role) -> Unit) {
    Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                "Cadastrar-se como",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            RoleCards(register)
        }
    }
}

@Composable
private fun RoleCards(select: (Role) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        listOf(Role.RESPONSAVEL to "Responsável", Role.FAMILIAR to "Familiar").forEach {
            (role, title) ->
            Surface(
                onClick = { select(role) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 3.dp,
            ) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        androidx.compose.ui.res.painterResource(
                            if (role == Role.RESPONSAVEL) br.com.vippela.R.drawable.avatar_parent
                            else br.com.vippela.R.drawable.avatar_family
                        ),
                        null,
                        Modifier.size(72.dp),
                    )
                    Text(title)
                }
            }
        }
    }
}
