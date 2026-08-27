package io.cloink.client.ui.home

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
}

data class HomeUiState(
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val fqdn: String = "",
    val address: String = "",
    val connectedPeers: Int = 0,
    val totalPeers: Int = 0,
) {
    val actionEnabled: Boolean
        get() = status == ConnectionStatus.CONNECTED || status == ConnectionStatus.DISCONNECTED
}
