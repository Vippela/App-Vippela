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
    val values =
        when (period) {
            0 -> {
                val parts = listOf(15, 20, 20, 5, 10, 20).map { state.selected.minutes * it / 100 }
                parts + (state.selected.minutes - parts.sum())
            }
            1 -> listOf(120, 180, 156, 20, 112, 180, state.selected.minutes)
            else -> listOf(710, 590, 630, 520)
        }
    val labels =
        when (period) {
            0 -> listOf("6h", "9h", "12h", "15h", "18h", "21h", "24h")
            1 -> listOf("seg", "ter", "qua", "qui", "sex", "sáb", "dom")
            else -> listOf("S1", "S2", "S3", "S4")
        }
    Page {
        Text(state.selected.name, style = MaterialTheme.typography.bodyMedium, color = Muted)
        Tabs(listOf("Dia", "Semana", "Mês"), period) { period = it }
        Panel {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("◷ Tempo de tela", style = MaterialTheme.typography.bodyMedium)
                Text(duration(values.sum()), style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                Modifier.fillMaxWidth().height(130.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
            ) {
                values.forEachIndexed { i, value ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            Modifier.width(20.dp)
                                .height((value.toFloat() / values.max().coerceAtLeast(1) * 90).dp)
                                .background(
                                    if (i % 2 == 0) Muted else MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(5.dp),
                                )
                                .semantics {
                                    contentDescription = "${labels[i]}: ${duration(value)}"
                                }
                        )
                        Text(labels[i], style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val allowed = state.apps.getValue(state.selectedId).count { it.allowed }
            listOf(
                    Triple("Tempo", .74f, Violet),
                    Triple("Apps", allowed / 4f, Orange),
                    Triple("Seguro", .9f, Color(0xFFE7B800)),
                )
                .forEach { (label, value, color) ->
                    Panel(Modifier.weight(1f)) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Ring(value, "", color, 56.dp)
                        }
                        Text(
                            label,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                }
        }
        Heading("Monitoramento de atividades")
        Panel {
            MenuRow(
                "Aplicativos mais usados",
                "YouTube, Instagram, TikTok",
                Icons.Outlined.PhoneAndroid,
            ) {
                go("apps")
            }
            HorizontalDivider()
            MenuRow(
                "${if(state.isParent) "Bloqueio" else "Liberação"} de aplicativos",
                "Veja os combinados da família",
                Icons.Outlined.Lock,
            ) {
                go(if (state.isParent) "requests" else "release")
            }
            HorizontalDivider()
            MenuRow(
                "Conteúdo acessado",
                "Orientações e alertas educativos",
                Icons.Outlined.Language,
            ) {
                go("alerts")
            }
        }
        if (period > 0)
            Panel {
                Text(
                    "Resumo ${if(period==1) "semanal" else "mensal"}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "O tempo de tela diminuiu em relação ao período anterior. Continue reservando momentos para atividades fora da tela."
                )
                Text(
                    "Dados ilustrativos da demonstração.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
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
