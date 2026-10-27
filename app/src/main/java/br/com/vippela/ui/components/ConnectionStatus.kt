package br.com.vippela.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import br.com.vippela.data.linking.model.DeviceLinkResponse
import br.com.vippela.ui.theme.Green
import br.com.vippela.ui.theme.Muted
import java.time.Instant
import kotlinx.coroutines.delay

internal fun recentContact(link: DeviceLinkResponse?, now: Instant): Boolean =
    link?.status == "active" && link.lastSeenAt?.let {
        runCatching { Instant.parse(it).let { seen -> !seen.isAfter(now.plusSeconds(5)) && seen.isAfter(now.minusSeconds(20)) } }.getOrDefault(false)
    } == true

@Composable
fun ConnectionStatus(link: DeviceLinkResponse?) {
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(link?.id) {
        while (true) { now = Instant.now(); delay(5000) }
    }
    val recent = recentContact(link, now)
    Text(
        when {
            link == null || link.status != "active" -> "Sem dispositivo vinculado"
            !recent -> "Sem confirmação recente do aparelho"
            !link.protectionEnabled -> "Online • proteção desativada"
            link.appliedRevision < link.revision -> "Online • aguardando aplicação das regras"
            else -> "Online • proteção ativa"
        },
        color = if (recent && link?.protectionEnabled == true) Green else Muted,
        style = MaterialTheme.typography.bodySmall,
    )
}
