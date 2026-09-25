package br.com.vippela.ui.linking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.vippela.data.linking.DeviceLinkRepository

class LinkingViewModelFactory(
    private val repository: DeviceLinkRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LinkingViewModel(repository) as T
    }
}