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
        Text("Exemplos de alertas educativos", style = MaterialTheme.typography.titleMedium, color = Muted)
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
        TodayUsage(state)
        Heading("Aplicativos mais usados")
        Panel {
            val report = state.currentLink?.report
            val zone = runCatching { java.time.ZoneId.of(report?.zone ?: "UTC") }.getOrDefault(java.time.ZoneId.of("UTC"))
            val today = java.time.LocalDate.now(zone).toString()
            val top = report?.buckets.orEmpty().filterKeys { it.substringBefore('|') == today }.entries
                .groupBy { it.key.substringAfterLast('|') }.mapValues { (_, values) -> values.sumOf { it.value } }.entries.sortedByDescending { it.value }.take(3)
            if (top.isEmpty()) Text("Aguardando estatísticas de uso do aparelho.")
            top.forEach { (pkg, millis) ->
                val label = state.currentLink?.apps?.firstOrNull { it.packageName == pkg }?.label ?: pkg
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LinkedAppIcon(label, report?.icons?.get(pkg))
                    Text(label, Modifier.weight(1f))
                    Text(if (millis < 60000) "< 1 min" else duration((millis / 60000).toInt()), style = MaterialTheme.typography.bodySmall)
                }
            }
            TextButton({ go("reports") }) { Text("Ver estatísticas →") }
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
        TodayUsage(state)
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
                    Text("Acompanhamento deste objetivo ainda não disponível.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun PerformanceScreen(state: DemoState, go: (String) -> Unit) = ReportsScreen(state, go)

@Composable
private fun TodayUsage(state: DemoState) {
    val report = state.currentLink?.report
    Panel {
        Text("Uso dos aplicativos hoje")
        if (report == null || !report.permission) Text("Aguardando permissão e estatísticas do familiar.")
        else {
            val zone = runCatching { java.time.ZoneId.of(report.zone) }.getOrDefault(java.time.ZoneId.of("UTC"))
            val today = java.time.LocalDate.now(zone).toString()
            val millis = report.buckets.filterKeys { it.substringBefore('|') == today }.values.sum()
            Text(if (millis in 1..59999) "< 1 min" else duration((millis / 60000).toInt()), style = MaterialTheme.typography.headlineLarge)
            Text("Dados registrados pelo aparelho. Veja o período e a atualização em Relatórios.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
