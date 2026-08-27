package io.cloink.client.ui.home

import io.cloink.client.ServiceAccessor
import io.cloink.client.StateListener
import io.cloink.client.StateListenerRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class HomeServiceGateway(
    private val serviceAccessor: ServiceAccessor,
    private val listenerRegistry: StateListenerRegistry,
    private val peerExecutor: ExecutorService = Executors.newSingleThreadExecutor(),
) : StateListener {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = mutableState.asStateFlow()
    private val started = AtomicBoolean(false)

    fun start() {
        if (started.compareAndSet(false, true)) {
            listenerRegistry.registerServiceStateListener(this)
        }
    }

    fun stop() {
        if (started.compareAndSet(true, false)) {
            listenerRegistry.unregisterServiceStateListener(this)
        }
    }

    fun close() {
        stop()
        peerExecutor.shutdownNow()
    }

    fun toggleConnection() {
        when (mutableState.value.status) {
            ConnectionStatus.CONNECTED -> {
                mutableState.update { it.copy(status = ConnectionStatus.DISCONNECTING) }
                serviceAccessor.switchConnection(false)
            }
            ConnectionStatus.DISCONNECTED -> {
                mutableState.update { it.copy(status = ConnectionStatus.CONNECTING) }
                serviceAccessor.switchConnection(true)
            }
            else -> Unit
        }
    }

    override fun onEngineStarted() = Unit

    override fun onEngineStopped() {
        mutableState.update {
            it.copy(
                status = ConnectionStatus.DISCONNECTED,
                connectedPeers = 0,
                totalPeers = 0,
            )
        }
    }

    override fun onAddressChanged(fqdn: String, address: String) {
        mutableState.update { it.copy(fqdn = fqdn, address = address) }
    }

    override fun onConnected() {
        mutableState.update { it.copy(status = ConnectionStatus.CONNECTED) }
    }

    override fun onConnecting() {
        mutableState.update { it.copy(status = ConnectionStatus.CONNECTING) }
    }

    override fun onDisconnected() {
        mutableState.update {
            it.copy(
                status = ConnectionStatus.DISCONNECTED,
                connectedPeers = 0,
                totalPeers = 0,
            )
        }
    }

    override fun onDisconnecting() {
        mutableState.update { it.copy(status = ConnectionStatus.DISCONNECTING) }
    }

    override fun onPeersListChanged(numberOfPeers: Long) {
        peerExecutor.execute {
            val peers = serviceAccessor.peersList
            var connected = 0
            for (index in 0L until peers.size()) {
                if (Status.fromLong(peers.get(index).connStatus) == Status.CONNECTED) {
                    connected++
                }
            }
            mutableState.update {
                it.copy(connectedPeers = connected, totalPeers = peers.size().toInt())
            }
        }
    }
}
