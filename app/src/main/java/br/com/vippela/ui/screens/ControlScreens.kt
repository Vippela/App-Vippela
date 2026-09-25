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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.vippela.ui.linking.LinkingUiState
import br.com.vippela.ui.linking.LinkingViewModel

@Composable
fun AppsScreen(state: DemoState, go: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    Page {
        Text(
            if (state.isParent) "Escolha os aplicativos permitidos para ${state.selected.name}."
            else "Confira seus aplicativos e os combinados da família.",
            color = Muted,
        )
        OutlinedTextField(
            query,
            { query = it },
            Modifier.fillMaxWidth(),
            label = { Text("Pesquisar aplicativos") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            singleLine = true,
        )
        val filtered =
            state.apps.getValue(state.selectedId).filter { it.name.contains(query, true) }
        if (filtered.isEmpty()) Panel { Text("Nenhum aplicativo encontrado.") }
        filtered.forEach { app ->
            Panel {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppSymbol(app.name)
                    Column(Modifier.weight(1f)) {
                        Text(app.name)
                        Text(
                            "${duration(app.minutes)} hoje",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                        Text(
                            if (app.allowed) "Permitido" else "Não permitido",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (app.allowed) Green else Muted,
                        )
                    }
                    if (state.isParent) Switch(app.allowed, { state.setAllowed(app.name, it) })
                }
                if (!state.isParent && !app.allowed)
                    SecondaryButton("Pedir liberação") { go("release/${app.name}") }
            }
        }
    }
}

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
fun RequestsScreen(state: DemoState) {
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
fun PairScreen(state: DemoState, viewModel: LinkingViewModel, done: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var code by rememberSaveable { mutableStateOf("") }

    val linked = uiState is LinkingUiState.Linked || state.linked
    val loading = uiState is LinkingUiState.Loading
    val error = uiState is LinkingUiState.Error
    val generatedToken = (uiState as? LinkingUiState.CodeReady)?.token

    LaunchedEffect(state.isParent) {
        if (state.isParent && uiState is LinkingUiState.Idle) {
            viewModel.generateCode()
        }
    }

    Page {
        Heading(if (state.isParent) "Gerar código" else "Vincule sua família")
        Panel {
            Icon(Icons.Outlined.Link, null, Modifier.size(56.dp), tint = Violet)
            Text(
                if (state.isParent) "Use este código no dispositivo do familiar."
                else "Digite o código informado pelo seu responsável."
            )
            if (state.isParent) {
                if (loading && generatedToken == null) {
                    CircularProgressIndicator()
                } else if (generatedToken != null) {
                    Text(generatedToken, style = MaterialTheme.typography.headlineLarge)
                } else if (error) {
                    TextButton(onClick = { viewModel.generateCode() }) {
                        Text("Tentar gerar código novamente")
                    }
                }
            }
        }
        if (!state.isParent) {
            OutlinedTextField(
                code,
                { code = it.filter(Char::isDigit).take(6) },
                Modifier.fillMaxWidth(),
                label = { Text("Código de 6 dígitos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = error,
                enabled = !loading,
                singleLine = true,
            )
        }
        if (error) Text("Confira o código e tente novamente.", color = Red)
        if (linked)
            Panel {
                Icon(Icons.Outlined.CheckCircle, null, tint = Green)
                Text("Dispositivo vinculado.")
            }
        if (loading && !state.isParent) {
            CircularProgressIndicator()
        } else {
            PrimaryButton(
                if (linked) "Continuar"
                else if (state.isParent) "Aguardando vínculo" else "Vincular dispositivo",
                enabled = linked || !state.isParent,
            ) {
                if (linked) {
                    done()
                } else {
                    val deviceId = android.provider.Settings.Secure.getString(
                        context.contentResolver,
                        android.provider.Settings.Secure.ANDROID_ID
                    )
                    viewModel.confirmCode(code, deviceId)
                }
            }
        }
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
