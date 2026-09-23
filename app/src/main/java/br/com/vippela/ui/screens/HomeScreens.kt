package br.com.vippela.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.vippela.data.*
import br.com.vippela.ui.components.*
import br.com.vippela.ui.theme.*

@Composable
fun HomeScreen(state: DemoState, go: (String) -> Unit) {
    if (state.isParent) ParentHome(state, go) else FamilyHome(state, go)
}

@Composable
private fun ParentHome(state: DemoState, go: (String) -> Unit) {
    var category by rememberSaveable { mutableIntStateOf(0) }
    Page {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Avatar(state.displayName, photo = state.photo)
            Column {
                Text("Bem-vindo ${state.displayName}", fontWeight = FontWeight.SemiBold)
                Text(
                    "Usuário Responsável",
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Panel {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Avatar(state.selected.name, 72.dp, state.photos["member:${state.selectedId}"])
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(state.selected.name, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Familiar • ${state.selected.age} anos",
                            style = MaterialTheme.typography.labelMedium,
                            color = Muted,
                        )
                        Text("● Online", color = Green, style = MaterialTheme.typography.bodySmall)
                        Button({ go("member") }, shape = RoundedCornerShape(10.dp)) {
                            Text(
                                "Verificar atividade →",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                state.members.forEach { member ->
                    TextButton({ state.selectedId = member.id }, Modifier.size(48.dp)) {
                        Text(if (state.selectedId == member.id) "●" else "○", color = Orange)
                    }
                }
            }
            PillTabs(listOf("Apps", "Planos", "Dicas", "Tela"), category) { category = it }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            val cards =
                when (category) {
                    0 ->
                        listOf(
                            Triple("Apps permitidos", Icons.Outlined.Apps, "apps"),
                            Triple("Pedidos de acesso", Icons.Outlined.Notifications, "requests"),
                        )
                    1 ->
                        listOf(
                            Triple("Limites e permissões", Icons.Outlined.FamilyRestroom, "limits"),
                            Triple("Gerenciar plano", Icons.Outlined.CreditCard, "plan"),
                        )
                    2 ->
                        listOf(
                            Triple("Aprender juntos", Icons.Outlined.School, "learn"),
                            Triple("Dicas de segurança", Icons.Outlined.Shield, "alerts"),
                        )
                    else ->
                        listOf(
                            Triple("Limite diário", Icons.Outlined.Timer, "limits"),
                            Triple("Tempo de tela", Icons.Outlined.BarChart, "reports"),
                        )
                }
            cards.forEach { (title, icon, route) ->
                Panel(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(icon, null, tint = Orange, modifier = Modifier.size(28.dp))
                        Text(
                            title,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Button(
                        { go(route) },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFE288),
                                contentColor = Purple,
                            ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(10.dp),
                    ) {
                        Text("Acessar  ›", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Text("Alertas do período", style = MaterialTheme.typography.titleMedium, color = Muted)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Baixo" to "12", "Atenção" to "4", "Alto" to "1").forEach { (risk, count) ->
                Surface(
                    Modifier.weight(1f).clickable { go("alerts/$risk") },
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 2.dp,
                ) {
                    Column(
                        Modifier.padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            count,
                            style = MaterialTheme.typography.titleLarge,
                            color =
                                when (risk) {
                                    "Baixo" -> Green
                                    "Alto" -> Red
                                    else -> Amber
                                },
                        )
                        Text(risk, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        SecondaryButton("Ver familiares") { go("family") }
    }
}

@Composable
private fun FamilyHome(state: DemoState, go: (String) -> Unit) {
    Page {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Avatar(
                state.selected.name,
                photo =
                    if (!state.isParent) state.photo else state.photos["member:${state.selectedId}"],
            )
            Column {
                Text(
                    "Olá, ${state.selected.name.substringBefore(' ')}!",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text("Vamos cuidar do seu tempo?", color = Muted)
            }
        }
        Panel {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Seu tempo hoje")
                    Text(
                        duration(state.selected.minutes),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        "de ${duration(state.limits.getValue(state.selectedId))} combinados",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Ring(
                    (state.selected.minutes.toFloat() / state.limits.getValue(state.selectedId))
                        .coerceAtMost(1f),
                    "do limite",
                )
            }
            Progress(state.selected.minutes.toFloat() / state.limits.getValue(state.selectedId))
            Text(
                "Uma pausa faz bem. Que tal uma atividade fora da tela?",
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Heading("Aplicativos mais usados")
        Panel {
            state.apps.getValue(state.selectedId).take(3).forEach { app ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppSymbol(app.name)
                    Text(app.name, Modifier.weight(1f))
                    Text(duration(app.minutes), style = MaterialTheme.typography.bodySmall)
                }
            }
            TextButton({ go("apps") }) { Text("Ver todos os aplicativos →") }
        }
        Heading("Seu aprendizado")
        Panel {
            MenuRow(
                "Aprenda sobre phishing",
                "15 minutos para se proteger",
                Icons.Outlined.PlayCircle,
            ) {
                go("learn")
            }
        }
        SecondaryButton("Ver meu desempenho") { go("performance") }
        SecondaryButton("Pedir liberação de app") { go("release") }
    }
}

@Composable
fun FamilyListScreen(state: DemoState, go: (String) -> Unit) {
    Page {
        Brand()
        Text("Selecione o perfil do familiar desejado", color = Muted)
        state.members.forEach { member ->
            Panel {
                Row(
                    Modifier.fillMaxWidth().clickable {
                        state.selectedId = member.id
                        go("member")
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Avatar(member.name, photo = state.photos["member:${member.id}"])
                    Column(Modifier.weight(1f)) {
                        Text(member.name, style = MaterialTheme.typography.titleMedium)
                        Text("● Online", color = Green, style = MaterialTheme.typography.bodySmall)
                    }
                    RiskBadge(member.risk)
                }
            }
        }
        SecondaryButton("Adicionar perfil") { go("add-member") }
        PrimaryButton("Vincular dispositivo") { go("pair") }
    }
}

@Composable
fun MemberScreen(state: DemoState, go: (String) -> Unit) {
    Page {
        Panel {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(state.selected.name, 72.dp, state.photos["member:${state.selectedId}"])
                Column {
                    Text(state.selected.name, style = MaterialTheme.typography.titleLarge)
                    Text("${state.selected.age} anos", color = Muted)
                    RiskBadge(state.selected.risk)
                }
            }
            Text(
                "● Dispositivo online • último uso agora",
                style = MaterialTheme.typography.labelMedium,
                color = Green,
            )
        }
        Heading("Hoje")
        Panel {
            Text("Tempo de tela")
            Text(duration(state.selected.minutes), style = MaterialTheme.typography.headlineLarge)
            Text(
                "de ${duration(state.limits.getValue(state.selectedId))} permitidos",
                color = Muted,
            )
            Progress(state.selected.minutes.toFloat() / state.limits.getValue(state.selectedId))
        }
        GoalCards(state)
        if (state.isParent) {
            PrimaryButton("Ajustar limite diário") { go("limits") }
            SecondaryButton("Aplicativos e permissões") { go("apps") }
        }
        SecondaryButton("Ver relatório semanal") { go("reports") }
    }
}

@Composable
fun GoalCards(state: DemoState) {
    Heading("Objetivos")
    Panel {
        val goals = state.goals[state.selectedId].orEmpty()
        if (goals.isEmpty()) Text("Nenhum objetivo adicionado ainda.")
        goals.forEachIndexed { index, goal ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (index == 1) Icons.Outlined.Bedtime else Icons.Outlined.School,
                    null,
                    tint = if (index == 1) Violet else Orange,
                )
                Column(Modifier.weight(1f)) {
                    Text(goal.title)
                    Text(
                        "Meta: ${duration(goal.minutes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    Progress(if (index == 1) .89f else .5f, if (index == 1) Violet else Orange)
                }
            }
        }
    }
}

@Composable
fun PerformanceScreen(state: DemoState, go: (String) -> Unit) {
    Page {
        Panel {
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Ring(.7f, "de redução")
                Column {
                    Text("Seu progresso", style = MaterialTheme.typography.titleMedium)
                    Text("Você está quase lá", style = MaterialTheme.typography.bodySmall)
                    TextButton({ go("reports") }) { Text("Mais detalhes") }
                }
            }
        }
        GoalCards(state)
        Heading("Progresso semanal")
        listOf("−3h de tempo de tela", "60% de tempo produtivo", "Aprenda sobre phishing")
            .forEach { title ->
                Panel {
                    Text(title)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("seg", "ter", "qua", "qui", "sex", "sáb", "dom").forEachIndexed {
                            i,
                            day ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(day, style = MaterialTheme.typography.bodySmall)
                                Text("●", color = if (i < 2) Purple else Lavender)
                            }
                        }
                    }
                }
            }
        PrimaryButton("Adicionar objetivo") { go("goal") }
    }
}
