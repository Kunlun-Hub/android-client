package io.cloink.client.ui.home

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.cloink.client.R
import io.cloink.client.ServiceAccessor
import io.cloink.client.StateListenerRegistry
import io.cloink.client.ui.theme.CloinkTheme

class BottomDialogFragment : BottomSheetDialogFragment() {
    private lateinit var serviceAccessor: ServiceAccessor
    private lateinit var listenerRegistry: StateListenerRegistry
    private lateinit var peersViewModel: PeersFragmentViewModel
    private lateinit var networksViewModel: NetworksFragmentViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        serviceAccessor = context as? ServiceAccessor ?: error("$context must implement ServiceAccessor")
        listenerRegistry = context as? StateListenerRegistry ?: error("$context must implement StateListenerRegistry")
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        peersViewModel = ViewModelProvider(this, PeersFragmentViewModel.getFactory(serviceAccessor))[PeersFragmentViewModel::class.java]
        networksViewModel = ViewModelProvider(this, NetworksFragmentViewModel.getFactory(serviceAccessor))[NetworksFragmentViewModel::class.java]
    }

    override fun onCreateDialog(state: Bundle?): Dialog = BottomSheetDialog(requireContext()).apply {
        setOnShowListener {
            findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { sheet ->
                BottomSheetBehavior.from(sheet).apply {
                    this.state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                }
                sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val peers by peersViewModel.uiState.observeAsState(PeersFragmentUiState(emptyList()))
                val networks by networksViewModel.uiState.observeAsState(NetworksFragmentUiState(emptyList(), emptyList()))
                CloinkTheme {
                    NetworkPanel(peers.peers, networks.resources) { resource, selected ->
                        runCatching {
                            if (selected) networksViewModel.selectRoute(resource.name)
                            else networksViewModel.deselectRoute(resource.name)
                        }.onFailure { Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show() }
                    }
                }
            }
        }

    override fun onStart() {
        super.onStart()
        listenerRegistry.registerServiceStateListener(peersViewModel.stateListener)
        listenerRegistry.registerServiceStateListener(networksViewModel)
        peersViewModel.onPeersChanged(0)
        networksViewModel.onPeersListChanged(0)
    }

    override fun onStop() {
        listenerRegistry.unregisterServiceStateListener(peersViewModel.stateListener)
        listenerRegistry.unregisterServiceStateListener(networksViewModel)
        super.onStop()
    }

}

@androidx.compose.runtime.Composable
internal fun NetworkPanel(
    peers: List<Peer>,
    resources: List<Resource>,
    onRouteSelection: (Resource, Boolean) -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    var search by remember { mutableStateOf("") }
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(tab == 0, { tab = 0; search = "" }, label = { Text(stringResource(R.string.peers_title)) })
                FilterChip(tab == 1, { tab = 1; search = "" }, label = { Text(stringResource(R.string.networks_title)) })
            }
            OutlinedTextField(
                search,
                { search = it },
                Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(if (tab == 0) R.string.peers_hint_search_peers else R.string.networks_hint_search_networks)) },
            )
            if (tab == 0) {
                PeerList(peers.filter { it.fqdn.contains(search, true) || it.ip.contains(search, true) })
            } else {
                ResourceList(
                    resources.filter { it.name.contains(search, true) || it.address.contains(search, true) },
                    onRouteSelection,
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun PeerList(peers: List<Peer>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(peers, key = { it.fqdn }) { peer ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(peer.fqdn, style = MaterialTheme.typography.titleMedium)
                        Text(statusLabel(peer.status), color = statusColor(peer.status))
                    }
                    Text(peer.ip, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (peer.ipv6.isNotBlank()) Text(peer.ipv6, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun ResourceList(resources: List<Resource>, onRouteSelection: (Resource, Boolean) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(resources, key = { it.name + it.address }) { resource ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(resource.name, style = MaterialTheme.typography.titleMedium)
                        Text(resource.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (resource.isExitNode) Text(stringResource(R.string.networks_desc_exit_node), color = MaterialTheme.colorScheme.primary)
                    }
                    Switch(
                        checked = resource.isSelected,
                        onCheckedChange = { onRouteSelection(resource, it) },
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun statusColor(status: Status) = when (status) {
    Status.CONNECTED -> MaterialTheme.colorScheme.secondary
    Status.CONNECTING -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@androidx.compose.runtime.Composable
private fun statusLabel(status: Status) = stringResource(
    when (status) {
        Status.CONNECTED -> R.string.peer_status_connected
        Status.CONNECTING -> R.string.peer_status_connecting
        Status.IDLE -> R.string.peer_status_idle
        Status.UNKNOWN -> R.string.peer_status_unknown
    },
)
