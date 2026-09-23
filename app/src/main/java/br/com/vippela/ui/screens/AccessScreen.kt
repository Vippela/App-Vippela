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
fun AccessScreen(
    state: DemoState,
    register: () -> Unit,
    login: (Role?) -> Unit,
    enter: () -> Unit,
) {
    val context = LocalContext.current
    val google = remember(context) { GoogleSignIn(context) }
    val scope = rememberCoroutineScope()
    var choosingGoogleRole by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    Page {
        Spacer(Modifier.height(32.dp))
        Brand()
        Text(
            "Acessar como",
            Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.headlineMedium,
        )
        RoleCards { login(it) }
        Spacer(Modifier.height(24.dp))
        PrimaryButton("Continuar com email") { login(null) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f))
            Text("  ou  ", color = Muted)
            HorizontalDivider(Modifier.weight(1f))
        }
        Button(
            { choosingGoogleRole = true },
            Modifier.fillMaxWidth(),
            enabled = !busy,
            shape = RoundedCornerShape(8.dp),
        ) {
            Image(
                androidx.compose.ui.res.painterResource(br.com.vippela.R.drawable.app_google),
                null,
                Modifier.size(20.dp)
                    .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(3.dp)),
            )
            Spacer(Modifier.width(12.dp))
            Text(if (busy) "Entrando…" else "Entrar com Google")
        }
        TextButton(
            { message = "A entrada com Facebook ainda não está disponível. Use e-mail ou Google." },
            Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Facebook")
        }
        Row(
            Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Não é cliente?", style = MaterialTheme.typography.bodySmall)
            TextButton(register) { Text("Cadastro") }
        }
        message?.let { Text(it, color = Muted, style = MaterialTheme.typography.bodySmall) }
    }
    if (choosingGoogleRole)
        AlertDialog(
            onDismissRequest = { choosingGoogleRole = false },
            title = { Text("Como vai usar a Vippela?") },
            text = {
                Column {
                    listOf(Role.RESPONSAVEL to "Responsável", Role.FAMILIAR to "Familiar")
                        .forEach { (role, title) ->
                            TextButton(
                                {
                                    choosingGoogleRole = false
                                    if (!google.isConfigured())
                                        message =
                                            "O login Google precisa ser ativado pela equipe do aplicativo. Por enquanto, entre com e-mail."
                                    else
                                        scope.launch {
                                            busy = true
                                            try {
                                                val profile = google.signIn()
                                                state.googleUid = profile.id
                                                state.role = role
                                                state.email = profile.email
                                                state.displayName = profile.name
                                                if (role == Role.FAMILIAR)
                                                    state.members[0] =
                                                        state.members[0].copy(name = profile.name)
                                                state.selectedId = 1
                                                enter()
                                            } catch (
                                                _:
                                                    androidx.credentials.exceptions.GetCredentialCancellationException) {
                                                message = "Entrada cancelada."
                                            } catch (_: Exception) {
                                                message =
                                                    "Não foi possível entrar com Google. Confira a conexão e tente novamente."
                                            } finally {
                                                busy = false
                                            }
                                        }
                                },
                                Modifier.fillMaxWidth(),
                            ) {
                                Text(title)
                            }
                        }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton({ choosingGoogleRole = false }) { Text("Cancelar") } },
        )
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
