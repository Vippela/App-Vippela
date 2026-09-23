package br.com.vippela.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import br.com.vippela.data.*
import br.com.vippela.ui.components.*
import br.com.vippela.ui.theme.*

@Composable
fun ProfileScreen(state: DemoState, go: (String) -> Unit, logout: () -> Unit) {
    Page {
        Panel {
            Avatar(state.currentName, 80.dp, state.photo)
            Text(state.currentName, style = MaterialTheme.typography.titleLarge)
            Text(if (state.isParent) "Responsável" else "Familiar", color = Muted)
            Text(state.contactEmail, style = MaterialTheme.typography.bodySmall)
            if (state.phone.isNotBlank())
                Text(state.phone, style = MaterialTheme.typography.bodySmall)
        }
        Panel {
            MenuRow("Editar perfil", "Nome e informações", Icons.Outlined.Person) {
                go("edit-profile")
            }
            if (state.isParent)
                MenuRow(
                    "Minha família",
                    "Adicionar e acompanhar familiares",
                    Icons.Outlined.FamilyRestroom,
                ) {
                    go("family")
                }
            MenuRow(
                "Vínculo familiar",
                if (state.linked) "Dispositivo vinculado" else "Vincular dispositivo",
                Icons.Outlined.Link,
            ) {
                go("pair")
            }
            MenuRow("Configurações", "Preferências do aplicativo", Icons.Outlined.Settings) {
                go("settings")
            }
        }
        if (!state.isParent) SecondaryButton("Meu desempenho") { go("performance") }
        SecondaryButton("Sair da conta", logout)
        Text(
            "Vippela • versão de demonstração 0.3.0",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
    }
}

@Composable
fun SettingsScreen(state: DemoState, go: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val rows =
        listOf(
            Triple("Editar perfil", "Foto, nome e informações", "edit-profile"),
            Triple("Conta", "Ativo • Protegido", "account"),
            Triple("E-mail e telefone", state.contactEmail, "contact"),
            Triple(
                "Limites de tempo de tela",
                "Defina limites de uso",
                if (state.isParent) "limits" else "member",
            ),
            Triple("Notificações", "Alertas e lembretes", "notification-settings"),
            Triple("Tema", "Aparência do aplicativo", "theme"),
        )
    Page {
        OutlinedTextField(
            query,
            { query = it },
            Modifier.fillMaxWidth(),
            placeholder = { Text("Pesquisar configurações…") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            singleLine = true,
        )
        listOf("Conta" to rows.take(3), "Acessibilidade" to rows.drop(3)).forEach { (heading, items)
            ->
            val matches =
                items.filter { it.first.contains(query, true) || it.second.contains(query, true) }
            if (matches.isNotEmpty()) {
                Heading(heading)
                Panel {
                    matches.forEach { (title, subtitle, route) ->
                        MenuRow(
                            title,
                            subtitle,
                            when (route) {
                                "edit-profile" -> Icons.Outlined.Person
                                "account" -> Icons.Outlined.AccountCircle
                                "contact" -> Icons.Outlined.Phone
                                "notification-settings" -> Icons.Outlined.Notifications
                                "theme" -> Icons.Outlined.Bedtime
                                else -> Icons.Outlined.Timer
                            },
                        ) {
                            go(route)
                        }
                    }
                }
            }
        }
        if (rows.none { it.first.contains(query, true) || it.second.contains(query, true) })
            Text("Nenhuma configuração encontrada.", color = Muted)
    }
}

@Composable
fun EditProfileScreen(state: DemoState, back: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(state.currentName) }
    var photo by rememberSaveable { mutableStateOf(state.photo) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val picker =
        androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
                photo = uri.toString()
            }
        }
    Page {
        Avatar(name, 80.dp, photo)
        TextButton({ picker.launch(arrayOf("image/*")) }) { Text("Alterar foto") }
        if (photo != null) TextButton({ photo = null }) { Text("Remover foto") }
        OutlinedTextField(
            name,
            { name = it },
            Modifier.fillMaxWidth(),
            label = { Text("Nome") },
            singleLine = true,
        )
        PrimaryButton("Salvar alterações", name.trim().length >= 2) {
            if (state.isParent) state.displayName = name.trim()
            else {
                val i = state.members.indexOfFirst { it.id == state.selectedId }
                state.members[i] = state.members[i].copy(name = name.trim())
            }
            state.photo = photo
            back()
        }
    }
}

@Composable
fun NotificationSettingsScreen(state: DemoState) {
    Page {
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Alertas educativos")
                    Text(
                        "Orientações sobre segurança digital",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
                Switch(state.notifications, { state.notifications = it })
            }
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Lembretes de pausa")
                    Text(
                        "Cuide do seu tempo",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
                Switch(state.reminders, { state.reminders = it })
            }
        }
        Text(
            "Preferências simuladas. Esta versão não envia notificações pelo Android.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
    }
}

@Composable
fun NotificationsScreen(state: DemoState, go: (String) -> Unit) {
    Page {
        if (state.notifications)
            Panel {
                MenuRow(
                    "Novo alerta educativo",
                    "Confira a orientação de segurança",
                    Icons.Outlined.Shield,
                ) {
                    go("alerts")
                }
            }
        if (state.reminders)
            Panel {
                MenuRow("Uma pausa faz bem", "Veja seu tempo de tela hoje", Icons.Outlined.Timer) {
                    go("reports")
                }
            }
        if (
            state.isParent &&
                state.requests.any { it.status == "Aguardando" && it.memberId == state.selectedId }
        )
            Panel {
                MenuRow(
                    "Pedido de liberação",
                    "Um familiar pediu acesso a um app",
                    Icons.Outlined.Apps,
                ) {
                    go("requests")
                }
            }
        if (!state.notifications && !state.reminders)
            Panel {
                Text("Alertas e lembretes desativados.")
                TextButton({ go("notification-settings") }) { Text("Ajustar preferências") }
            }
    }
}

@Composable
fun InfoScreen(route: String, state: DemoState) {
    Page {
        when (route) {
            "account" ->
                Panel {
                    Icon(Icons.Outlined.VerifiedUser, null, tint = Violet)
                    Heading("Sua conta")
                    Text(state.currentName)
                    Text(state.contactEmail)
                    if (state.phone.isNotBlank()) Text(state.phone)
                    Text("Perfil: ${if(state.isParent) "Responsável" else "Familiar"}")
                    Text(
                        "Conta de demonstração. A autenticação real será implementada na integração com o backend.",
                        color = Muted,
                    )
                }
            "contact" -> ContactEditor(state)
            "theme" ->
                Panel {
                    Heading("Aparência")
                    listOf(false to "Claro", true to "Escuro").forEach { (dark, title) ->
                        Row(
                            Modifier.fillMaxWidth().clickable { state.darkMode = dark },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(state.darkMode == dark, { state.darkMode = dark })
                            Text(title)
                        }
                    }
                }
            "plan" ->
                Panel {
                    Icon(Icons.Outlined.CreditCard, null, tint = Orange)
                    Heading("Seu plano familiar")
                    Text("Demonstração", style = MaterialTheme.typography.headlineMedium)
                    Text("Explore o acompanhamento familiar, as trilhas e os relatórios.")
                    Text("Não há assinatura ou cobrança nesta versão.", color = Muted)
                }
        }
    }
}

@Composable
private fun ContactEditor(state: DemoState) {
    var address by rememberSaveable { mutableStateOf(state.contactEmail) }
    var phone by rememberSaveable { mutableStateOf(state.phone) }
    var saved by rememberSaveable { mutableStateOf(false) }
    Panel {
        Heading("E-mail e telefone")
        OutlinedTextField(
            address,
            {
                address = it
                saved = false
            },
            Modifier.fillMaxWidth(),
            label = { Text("E-mail de contato") },
            singleLine = true,
        )
        OutlinedTextField(
            phone,
            {
                phone = it
                saved = false
            },
            Modifier.fillMaxWidth(),
            label = { Text("Telefone") },
            singleLine = true,
        )
        PrimaryButton(
            "Salvar contatos",
            android.util.Patterns.EMAIL_ADDRESS.matcher(address.trim()).matches(),
        ) {
            state.contactEmail = address.trim()
            state.phone = phone.trim()
            saved = true
        }
        if (saved) Text("Contatos atualizados.")
        Text(
            "Alteração apenas neste aplicativo. Seu e-mail de acesso permanece igual.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
    }
}
