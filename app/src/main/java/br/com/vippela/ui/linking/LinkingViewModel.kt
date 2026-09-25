package br.com.vippela.ui.linking

import br.com.vippela.data.linking.DeviceLinkRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LinkingViewModel(private val repository: DeviceLinkRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<LinkingUiState>(LinkingUiState.Idle)
    val uiState: StateFlow<LinkingUiState> = _uiState

    fun generateCode() {
        viewModelScope.launch {
            _uiState.value = LinkingUiState.Loading
            repository.generateCode().fold(
                onSuccess = { _uiState.value = LinkingUiState.CodeReady(it.token, it.expiresAt) },
                onFailure = { _uiState.value = LinkingUiState.Error(it.message ?: "Erro") }
            )
        }
    }

    fun confirmCode(token: String, deviceId: String) {
        viewModelScope.launch {
            _uiState.value = LinkingUiState.Loading
            repository.confirmCode(token, deviceId).fold(
                onSuccess = { _uiState.value = LinkingUiState.Linked(it) },
                onFailure = { _uiState.value = LinkingUiState.Error(it.message ?: "Erro") }
            )
        }
    }
}