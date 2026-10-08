package br.com.vippela.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.com.vippela.blocking.AppBlockService
import br.com.vippela.data.DemoState
import br.com.vippela.ui.components.*
import br.com.vippela.ui.theme.*
import java.time.Instant
import kotlinx.coroutines.delay

@Composable
fun LinkedAppsScreen(state: DemoState, go: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    Page {
        val link = state.currentLink
        if (link == null) {
            Text(
                if (state.isParent)
                    "Vincule o aparelho de ${state.selected.name} para controlar seus aplicativos."
                else "Vincule este aparelho ao seu responsável para receber os combinados."
            )
            PrimaryButton("Vínculo familiar") { go("pair") }
        } else {
            Text(
                if (state.isParent) "Aplicativos de ${link.memberName}"
                else "Aplicativos deste aparelho",
                style = MaterialTheme.typography.titleLarge,
            )
            var now by remember { mutableStateOf(Instant.now()) }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(5000)
                    now = Instant.now()
                }
            }
            val online =
                link.lastSeenAt?.let {
                    runCatching { Instant.parse(it).isAfter(now.minusSeconds(20)) }
                        .getOrDefault(false)
                } == true
            Panel {
                Text(
                    when {
                        !online ->
                            "Aparelho sem confirmação recente. As novas regras aguardam conexão."
                        !link.protectionEnabled ->
                            "Proteção desativada no familiar. Ative-a para aplicar os bloqueios."
                        link.appliedRevision < link.revision ->
                            "Regra salva. Aguardando confirmação do familiar…"
                        else -> "Proteção ativa • regras recebidas pelo familiar"
                    }
                )
            }
            if (!state.isParent) { ProtectionSetup(); UsagePermissionCard() }
            OutlinedTextField(
                query,
                { query = it },
                Modifier.fillMaxWidth(),
                label = { Text("Pesquisar aplicativos") },
                singleLine = true,
            )
            val filtered = link.apps.filter { it.label.contains(query, true) }
            if (link.apps.isEmpty())
                Text("Abra a Vippela no familiar para enviar a lista de aplicativos.")
            else if (filtered.isEmpty()) Text("Nenhum aplicativo encontrado.")
            filtered.forEach { app ->
                Panel {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        LinkedAppIcon(app.label, link.report?.icons?.get(app.packageName))
                        Column(Modifier.weight(1f)) {
                            Text(app.label)
                            Text(
                                if (app.packageName in link.blockedPackages) "Bloqueio solicitado"
                                else "Permitido",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (state.isParent)
                            Switch(
                                app.packageName !in link.blockedPackages,
                                { state.changeRemoteRule(app.packageName, !it) },
                                enabled = !state.linkBusy,
                            )
                    }
                }
            }
            Text(
                "Apps essenciais do sistema não aparecem nesta lista.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        RemoteError(state)
    }
}

@Composable
fun ServerConfiguration(state: DemoState) {
    var editing by remember(state.serverAddress) { mutableStateOf(state.serverAddress.isBlank()) }
    var address by remember(state.serverAddress) { mutableStateOf(state.serverAddress) }
    if (editing) {
        Panel {
            Text("Servidor da família", style = MaterialTheme.typography.titleMedium)
            Text("Use o mesmo endereço nos dois celulares.")
            OutlinedTextField(
                address,
                { address = it },
                Modifier.fillMaxWidth(),
                label = { Text("Endereço do servidor") },
                placeholder = { Text("https://servidor.exemplo.com/") },
                singleLine = true,
            )
            PrimaryButton("Salvar endereço") { if (state.configureServer(address)) editing = false }
        }
    } else TextButton({ editing = true }) { Text("Configurar servidor") }
}

@Composable
fun RemoteError(state: DemoState) {
    state.linkError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    if (state.linkBusy) LinearProgressIndicator(Modifier.fillMaxWidth())
}

@Composable
fun ProtectionSetup() {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(AppBlockService.connected) }
    var consent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            enabled = AppBlockService.connected
            delay(1000)
        }
    }
    Panel {
        Text(
            if (enabled) "Proteção ativada neste aparelho" else "Ative a proteção neste aparelho",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            "A Vippela identifica o app aberto e retorna à tela inicial se ele estiver bloqueado pelo responsável vinculado. Não lê o conteúdo das telas. Os nomes dos apps elegíveis e a confirmação das regras são enviados ao servidor da família."
        )
        Text(
            "As últimas regras recebidas continuam valendo sem internet enquanto a proteção estiver ativa."
        )
        SecondaryButton(if (enabled) "Configurações da proteção" else "Ativar proteção") {
            consent = true
        }
    }
    if (consent)
        AlertDialog(
            onDismissRequest = { consent = false },
            title = { Text("Proteção familiar") },
            text = {
                Text(
                    "Para bloquear apps, ative ‘Proteção familiar Vippela’ em Acessibilidade. Você pode desativar esse acesso nas configurações do Android."
                )
            },
            confirmButton = {
                TextButton({
                    consent = false
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }) {
                    Text("Continuar")
                }
            },
            dismissButton = { TextButton({ consent = false }) { Text("Agora não") } },
        )
}
