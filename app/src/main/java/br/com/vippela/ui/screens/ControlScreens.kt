package br.com.vippela.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.vippela.data.*
import br.com.vippela.ui.components.*
import br.com.vippela.ui.theme.*

@Composable fun AppsScreen(state: DemoState, go: (String) -> Unit) = LinkedAppsScreen(state, go)

@Composable
fun LimitsScreen(state: DemoState, back: () -> Unit) {
    var minutes by
        rememberSaveable(state.selectedId) {
            mutableFloatStateOf(state.limits.getValue(state.selectedId).toFloat())
        }
    Page {
        Text(state.selected.name, style = MaterialTheme.typography.titleLarge)
        Panel {
            Icon(Icons.Outlined.Timer, null, tint = Violet, modifier = Modifier.size(50.dp))
            Text("Limite diário", style = MaterialTheme.typography.titleLarge)
            Text(duration(minutes.toInt()), style = MaterialTheme.typography.headlineLarge)
            Slider(minutes, { minutes = it }, valueRange = 30f..480f, steps = 14)
            Text(
                "Combine um tempo que reserve espaço para estudar, descansar e brincar.",
                color = Muted,
            )
        }
        Text(
            "O limite será aplicado apenas aos dados de demo.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
        PrimaryButton("Salvar limite") {
            state.limits[state.selectedId] = (minutes / 30).toInt() * 30
            back()
        }
    }
}

@Composable
fun ReleaseScreen(state: DemoState, initialApp: String) {
    if (state.linked) {
        Page {
            Heading("Liberação de aplicativo")
            Text(
                "Converse com seu responsável. Ele pode liberar o aplicativo pela tela Aplicativos no celular dele."
            )
        }
        return
    }
    var app by rememberSaveable { mutableStateOf(initialApp) }
    var message by rememberSaveable { mutableStateOf("") }
    var sent by rememberSaveable { mutableStateOf(false) }
    val current = state.apps.getValue(state.selectedId).first { it.name == app }
    val pending =
        state.requests.any {
            it.memberId == state.selectedId && it.app == app && it.status == "Aguardando"
        }
    Page {
        Heading("Liberação de app")
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.apps.getValue(state.selectedId).forEach { a ->
                FilterChip(
                    app == a.name,
                    {
                        app = a.name
                        sent = false
                    },
                    label = { Text(a.name) },
                )
            }
        }
        Panel {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppSymbol(app)
                Column {
                    Text(app, style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (current.allowed) "Este app está na sua lista de permitidos."
                        else
                            "Esse app não está na sua lista de permitidos. Mas você pode pedir para liberar!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }
        }
        Panel {
            Text("Escreva um recado para quem cuida de você (opcional)")
            OutlinedTextField(
                message,
                { message = it },
                Modifier.fillMaxWidth(),
                placeholder = { Text("Ex.: usar o YouTube para estudar") },
                minLines = 2,
            )
            listOf("Para estudar", "Para o dever de casa", "Só mais 30 min").forEach { suggestion ->
                SuggestionChip({ message = suggestion }, label = { Text(suggestion) })
            }
            PrimaryButton(
                if (current.allowed) "App já liberado"
                else if (pending) "Pedido aguardando resposta" else "Enviar pedido",
                !current.allowed && !pending,
            ) {
                state.requestApp(app, message)
                sent = true
            }
            if (sent) Text("Pedido enviado para o responsável.", color = Green)
        }
        Heading("Seus pedidos")
        RequestCards(state, false)
    }
}

@Composable
fun RequestsScreen(state: DemoState, go: (String) -> Unit) {
    if (state.linked) {
        Page {
            Text(
                "As permissões de ${state.selected.name} são controladas pela lista de aplicativos do aparelho vinculado."
            )
            PrimaryButton("Ver aplicativos") { go("apps") }
        }
        return
    }
    Page {
        Text("Acompanhe os pedidos da família.", color = Muted)
        RequestCards(state, true)
    }
}

@Composable
private fun RequestCards(state: DemoState, parent: Boolean) {
    val requests = state.requests.filter { it.memberId == state.selectedId }
    if (requests.isEmpty()) Panel { Text("Nenhum pedido por aqui ainda.") }
    requests.reversed().forEach { req ->
        Panel {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppSymbol(req.app)
                Column {
                    Text(req.app, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "● ${req.status}",
                        color =
                            if (req.status == "Liberado") Green
                            else if (req.status == "Não liberado") Red else Amber,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (req.message.isNotBlank()) Text(req.message, color = Muted)
            if (parent && req.status == "Aguardando") {
                PrimaryButton("Liberar aplicativo") { state.decide(req.id, true) }
                SecondaryButton("Não liberar agora") { state.decide(req.id, false) }
            }
        }
    }
}

@Composable
fun AddMemberScreen(state: DemoState, done: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    Page {
        Heading("Adicionar familiar")
        Text("Vamos incluir mais alguém na sua família.", color = Muted)
        OutlinedTextField(
            name,
            { name = it },
            Modifier.fillMaxWidth(),
            label = { Text("Nome do familiar") },
            singleLine = true,
        )
        OutlinedTextField(
            age,
            { age = it.filter(Char::isDigit).take(2) },
            Modifier.fillMaxWidth(),
            label = { Text("Idade") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        PrimaryButton(
            "Adicionar e vincular",
            name.trim().length >= 2 && (age.toIntOrNull() ?: 0) in 1..99,
        ) {
            state.addMember(name.trim(), age.toInt())
            done()
        }
    }
}

@Composable
fun PairScreen(state: DemoState, done: () -> Unit) {
    Page {
        Heading("Vincular ${state.selected.name}")
        ServerConfiguration(state)
        val link = state.currentLink
        if (link != null) {
            Panel {
                Icon(Icons.Outlined.CheckCircle, null, tint = Green)
                Text("Dispositivo vinculado a ${link.memberName}.")
            }
            PrimaryButton("Continuar", onClick = done)
        } else {
            Panel {
                Text("Gere um código e digite-o no celular do familiar.")
                state.linkCode
                    ?.takeIf { it.memberKey == state.selectedId.toString() }
                    ?.let {
                        Text(it.token, style = MaterialTheme.typography.headlineLarge)
                        Text("Válido por 5 minutos. A confirmação aparecerá aqui automaticamente.")
                    }
            }
            PrimaryButton(
                if (state.linkCode == null) "Gerar código" else "Gerar novo código",
                !state.linkBusy,
            ) {
                state.generateLinkCode()
            }
        }
        RemoteError(state)
    }
}

@Composable
fun GoalScreen(state: DemoState, back: () -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var minutes by rememberSaveable { mutableStateOf("30") }
    Page {
        Heading("Um novo objetivo")
        OutlinedTextField(
            title,
            { title = it },
            Modifier.fillMaxWidth(),
            label = { Text("O que você quer fazer?") },
            singleLine = true,
        )
        OutlinedTextField(
            minutes,
            { minutes = it.filter(Char::isDigit).take(3) },
            Modifier.fillMaxWidth(),
            label = { Text("Meta em minutos por dia") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        PrimaryButton(
            "Salvar objetivo",
            title.isNotBlank() && (minutes.toIntOrNull() ?: 0) in 1..999,
        ) {
            state.goals[state.selectedId] =
                state.goals[state.selectedId].orEmpty() + Goal(title.trim(), minutes.toInt())
            back()
        }
    }
}
