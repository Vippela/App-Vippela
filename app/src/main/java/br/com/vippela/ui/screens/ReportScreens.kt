package br.com.vippela.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import br.com.vippela.data.*
import br.com.vippela.ui.components.*
import br.com.vippela.ui.theme.*

@Composable
fun ReportsScreen(state: DemoState, go: (String) -> Unit) {
    var period by rememberSaveable { mutableIntStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var localPermission by remember { mutableStateOf(br.com.vippela.data.usage.UsageCollector.permitted(context)) }
    LaunchedEffect(state.isParent) {
        if (!state.isParent) while (true) {
            localPermission = br.com.vippela.data.usage.UsageCollector.permitted(context)
            kotlinx.coroutines.delay(1000)
        }
    }
    val link = state.currentLink
    val report = link?.report
    val zone = runCatching { java.time.ZoneId.of(report?.zone ?: "UTC") }.getOrDefault(java.time.ZoneId.of("UTC"))
    val today = java.time.LocalDate.now(zone)
    val start = today.minusDays(if (period == 0) 0 else if (period == 1) 6 else 29)
    val buckets = report?.buckets.orEmpty().filterKeys { it.substringBefore('|') >= start.toString() && it.substringBefore('|') <= today.toString() }
    val totals = LongArray(if (period == 0) 6 else if (period == 1) 7 else 6)
    buckets.forEach { (key, value) ->
        val parts = key.split('|')
        val date = runCatching { java.time.LocalDate.parse(parts[0]) }.getOrNull()
        val hour = parts.getOrNull(1)?.toIntOrNull()
        val index = if (period == 0) (hour ?: -4) / 4 else date?.let { java.time.temporal.ChronoUnit.DAYS.between(start, it).toInt() / if (period == 1) 1 else 5 } ?: -1
        if (index in totals.indices) totals[index] += value
    }
    fun time(ms: Long) = if (ms in 1..59999) "< 1 min" else duration((ms / 60000).toInt())
    Page {
        Text(link?.memberName ?: if (state.isParent) state.selected.name else "Seu uso", style = MaterialTheme.typography.titleMedium)
        if (!state.isParent) UsagePermissionCard()
        link?.reportError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (link == null) {
            Text("Vincule o aparelho do familiar para consultar estatísticas reais.")
            PrimaryButton("Vínculo familiar") { go("pair") }
        } else if (!state.isParent && !localPermission) {
            Text("Permita o acesso ao uso neste aparelho. A acessibilidade para bloquear apps é uma permissão separada.")
        } else if (report == null || report.collectedAt == 0L) {
            Text("Aguardando o primeiro relatório do familiar. Atualize também o backend e abra a Vippela no aparelho vinculado.")
        } else if (!report.permission) {
            Text(if (!state.isParent && localPermission) "Permissão ativada. Preparando as estatísticas deste aparelho…"
                else "No último relatório, o acesso ao uso estava desativado no familiar. Se já foi ativado, aguarde a sincronização e confira a conexão dos aparelhos.")
        } else {
            Tabs(listOf("Dia", "7 dias", "30 dias"), period) { period = it }
            Panel {
                Text("Uso dos aplicativos monitorados", style = MaterialTheme.typography.titleMedium)
                Text(time(totals.sum()), style = MaterialTheme.typography.headlineMedium)
                Row(Modifier.fillMaxWidth().height(145.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                    totals.forEachIndexed { index, value ->
                        val label = if (period == 0) "${index * 4}h" else start.plusDays((index * if (period == 1) 1 else 5).toLong()).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.width(18.dp).height((value.toFloat() / (totals.maxOrNull() ?: 1).coerceAtLeast(1) * 100).dp).background(Violet, RoundedCornerShape(5.dp)).semantics { contentDescription = "$label: ${time(value)}" })
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                if (totals.sum() == 0L) Text("Ainda não há uso registrado neste período.")
                Text("Histórico desde " + java.time.Instant.ofEpochMilli(report.since).atZone(zone).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")), style = MaterialTheme.typography.bodySmall)
                Text("Atualizado em " + java.time.Instant.ofEpochMilli(report.collectedAt).atZone(zone).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")) + " • " + report.zone, style = MaterialTheme.typography.bodySmall)
                Text("Dias sem coleta não representam necessariamente ausência de uso. Apps essenciais não entram no total.", style = MaterialTheme.typography.bodySmall)
            }
            val allowed = link.apps.count { it.packageName !in link.blockedPackages }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(Triple("Permitidos", allowed, Violet), Triple("Bloqueados", link.apps.size - allowed, Orange)).forEach { (label, count, color) ->
                    Panel(Modifier.weight(1f)) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Ring(if (link.apps.isEmpty()) 0f else count.toFloat() / link.apps.size, "", color, 64.dp)
                        }
                        Text("$count $label", style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
            Heading("Aplicativos mais usados")
            val byApp = buckets.entries.groupBy { it.key.substringAfterLast('|') }.mapValues { (_, values) -> values.sumOf { it.value } }
            byApp.entries.sortedByDescending { it.value }.take(10).forEach { (pkg, millis) ->
                val label = link.apps.firstOrNull { it.packageName == pkg }?.label ?: pkg
                Panel {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LinkedAppIcon(label, report.icons[pkg])
                        Text(label, Modifier.weight(1f))
                        Text(time(millis))
                    }
                }
            }
        }
        if (link != null) SecondaryButton("Gerenciar aplicativos") { go("apps") }
    }
}

private data class Alert(val risk: String, val title: String, val detail: String)

private val alerts =
    listOf(
        Alert(
            "Alto",
            "Link suspeito identificado",
            "Uma mensagem ofereceu um prêmio e pediu dados pessoais. Não informe senhas ou códigos. Confira a origem pelo canal oficial e peça ajuda a alguém de confiança.",
        ),
        Alert(
            "Atenção",
            "Hora de fazer uma pausa",
            "O tempo de uso está próximo do limite combinado. Que tal descansar os olhos e reservar um momento para uma atividade fora da tela?",
        ),
        Alert(
            "Baixo",
            "Você está aprendendo!",
            "Continuar uma trilha educativa ajuda a reconhecer situações de risco. Compartilhe uma dica nova com sua família.",
        ),
    )

@Composable
fun AlertsScreen(filter: String, go: (String) -> Unit) {
    var selected by rememberSaveable { mutableStateOf(filter) }
    Page {
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("Todos", "Baixo", "Atenção", "Alto").forEach { risk ->
                FilterChip(selected == risk, { selected = risk }, label = { Text(risk) })
            }
        }
        alerts.forEachIndexed { index, alert ->
            if (selected == "Todos" || selected == alert.risk)
                Panel {
                    RiskBadge(alert.risk)
                    Text(alert.title, style = MaterialTheme.typography.titleMedium)
                    Text(alert.detail.take(92) + "…", color = Muted)
                    TextButton({ go("alert/$index") }) { Text("Ver orientação →") }
                }
        }
    }
}

@Composable
fun AlertDetailScreen(index: Int, go: (String) -> Unit) {
    val alert = alerts[index]
    Page {
        RiskBadge(alert.risk)
        Text(alert.title, style = MaterialTheme.typography.headlineMedium)
        Panel {
            Icon(Icons.Outlined.Shield, null, Modifier.size(48.dp), tint = Violet)
            Text(alert.detail)
        }
        PrimaryButton("Aprender mais") { go("learn") }
    }
}
