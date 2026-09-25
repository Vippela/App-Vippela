package br.com.vippela.ui.linking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun GenerateLinkScreen(viewModel: LinkingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.generateCode()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = uiState) {
            is LinkingUiState.Idle,
            is LinkingUiState.Loading -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Gerando código...")
            }

            is LinkingUiState.CodeReady -> {
                Text(
                    text = "Código de vínculo",
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.token,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Válido por alguns minutos",
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(24.dp))
                OutlinedButton(onClick = { viewModel.generateCode() }) {
                    Text("Gerar novo código")
                }
            }

            is LinkingUiState.Error -> {
                Text(text = "Erro: ${state.message}")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.generateCode() }) {
                    Text("Tentar novamente")
                }
            }

            else -> Unit // Linked não se aplica nesta tela
        }
    }
}