package br.com.vippela.ui.screens

import android.provider.Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.vippela.data.DemoState
import br.com.vippela.ui.components.*
import br.com.vippela.ui.linking.LinkingUiState
import br.com.vippela.ui.linking.LinkingViewModel
import br.com.vippela.ui.theme.*

@Composable
fun FamilyConnectionScreen(
    state: DemoState,
    back: () -> Unit,
    done: () -> Unit,
    viewModel: LinkingViewModel,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var code by rememberSaveable { mutableStateOf("") }
    var help by rememberSaveable { mutableStateOf(false) }

    val connected = uiState is LinkingUiState.Linked || state.linked
    val loading = uiState is LinkingUiState.Loading
    val error = uiState is LinkingUiState.Error

    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!connected) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(back) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Voltar") }
                Text("Vamos conectar\ncom sua família", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Peça para o seu responsável que digite o código aqui.",
                color = Muted,
                textAlign = TextAlign.Center,
            )
        }
        androidx.compose.foundation.Image(
            androidx.compose.ui.res.painterResource(br.com.vippela.R.drawable.family_connection),
            "Responsável ajudando a criança no skate",
            Modifier.fillMaxWidth().height(200.dp).padding(vertical = 12.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
        )
        if (connected) {
            Text(
                "Você está conectado\ncom sua família!",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "Agora você e ${state.displayName} têm um vínculo!",
                color = Muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(30.dp))
            Panel {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Avatar(state.displayName, 40.dp, state.photos["parent"])
                    Column {
                        Text("Conectado a")
                        Text(
                            state.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.Home, null, tint = Orange)
                    Text(
                        "Um passo para o uso consciente da Internet",
                        color = Orange,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        } else {
            OutlinedTextField(
                code,
                { code = it.filter(Char::isDigit).take(6) },
                Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !loading,
                placeholder = {
                    Text(
                        "— — — — — —",
                        Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Orange,
                    )
                },
                textStyle =
                    MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center,
                        letterSpacing = 8.sp,
                    ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = error,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Orange,
                        focusedBorderColor = Orange,
                    ),
            )
            if (error) Text("Confira o código e tente novamente.", color = Red)
        }
        Spacer(Modifier.height(48.dp))
        if (loading) {
            CircularProgressIndicator()
        } else {
            PrimaryButton(if (connected) "Começar" else "Conectar", connected || code.length == 6) {
                if (connected) {
                    done()
                } else {
                    val deviceId = Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                    viewModel.confirmCode(code, deviceId)
                }
            }
        }
        TextButton({ help = true }) {
            Text(
                if (connected) "Saiba como funciona" else "Seu código não está funcionando?",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(Modifier.height(32.dp))
        StepIndicator(if (connected) 1 else 0, 2)
    }
    if (help)
        AlertDialog(
            onDismissRequest = { help = false },
            title = { Text(if (connected) "Vínculo familiar" else "Ajuda com o código") },
            text = {
                Text(
                    if (connected)
                        "Sua família pode combinar limites e acompanhar atividades. Os dados desta versão são demonstrativos."
                    else
                        "Peça o código ao responsável em Vínculo familiar."
                )
            },
            confirmButton = { TextButton({ help = false }) { Text("Entendi") } },
        )
}