package br.com.vippela.ui.linking
import br.com.vippela.data.linking.model.DeviceLinkResponse
sealed class LinkingUiState {
    object Idle : LinkingUiState()
    object Loading : LinkingUiState()
    data class CodeReady(val token: String, val expiresAt: String) : LinkingUiState()
    data class Linked(val response: DeviceLinkResponse) : LinkingUiState()
    data class Error(val message: String) : LinkingUiState()
}