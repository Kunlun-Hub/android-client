package io.cloink.client.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(
    private val gateway: HomeServiceGateway,
) : ViewModel() {
    val state: StateFlow<HomeUiState> = gateway.state

    fun start() {
        gateway.start()
    }

    fun stop() {
        gateway.stop()
    }

    fun toggleConnection() {
        gateway.toggleConnection()
    }

    override fun onCleared() {
        gateway.close()
    }

    class Factory(
        private val gateway: HomeServiceGateway,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HomeViewModel::class.java))
            return HomeViewModel(gateway) as T
        }
    }
}
