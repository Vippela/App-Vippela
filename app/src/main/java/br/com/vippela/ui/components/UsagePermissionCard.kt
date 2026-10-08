package br.com.vippela.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import br.com.vippela.data.usage.UsageCollector
import kotlinx.coroutines.delay

@Composable
fun UsagePermissionCard() {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(UsageCollector.permitted(context)) }
    var explain by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) { granted = UsageCollector.permitted(context); delay(1500) }
    }
    Panel {
        Text(if (granted) "Estatísticas de uso ativadas" else "Compartilhar tempo de uso", style = MaterialTheme.typography.titleMedium)
        Text("Esta permissão é diferente da acessibilidade usada no bloqueio. Com sua permissão, a Vippela compartilha o tempo de uso por aplicativo com o responsável vinculado. Não lê mensagens nem o conteúdo das telas. O histórico começa após a ativação.")
        SecondaryButton(if (granted) "Gerenciar acesso ao uso" else "Permitir estatísticas") { explain = true }
    }
    if (explain) AlertDialog(
        onDismissRequest = { explain = false },
        title = { Text("Acesso às estatísticas") },
        text = { Text("Na próxima tela, selecione Vippela e permita o acesso ao uso. Você pode revogar essa permissão quando quiser. Os relatórios são enviados ao servidor da família.") },
        confirmButton = { TextButton({ explain = false; context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }) { Text("Continuar") } },
        dismissButton = { TextButton({ explain = false }) { Text("Agora não") } },
    )
}
