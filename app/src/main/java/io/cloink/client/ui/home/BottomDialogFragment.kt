package io.cloink.client.ui.home

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.graphics.Color
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.cloink.client.R
import io.cloink.client.ServiceAccessor
import io.cloink.client.StateListenerRegistry
import io.cloink.client.ui.theme.CloinkTheme
import io.cloink.client.ui.theme.ThemeRuntime

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
            window?.let { dialogWindow ->
                val dark = when (ThemeRuntime.mode) {
                    AppCompatDelegate.MODE_NIGHT_YES -> true
                    AppCompatDelegate.MODE_NIGHT_NO -> false
                    else -> resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
                }
                WindowCompat.setDecorFitsSystemWindows(dialogWindow, false)
                val systemBarColor = Color.parseColor(if (dark) "#151719" else "#F5F6F7")
                dialogWindow.statusBarColor = systemBarColor
                dialogWindow.navigationBarColor = systemBarColor
                WindowCompat.getInsetsController(dialogWindow, dialogWindow.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { sheet ->
                BottomSheetBehavior.from(sheet).apply {
                    this.state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                    isDraggable = false
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
                    NetworkPanel(
                        peers.peers,
                        networks.resources,
                        onDismiss = ::dismiss,
                    ) { resource, selected ->
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
    onDismiss: () -> Unit = {},
    onRouteSelection: (Resource, Boolean) -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    var search by remember { mutableStateOf("") }
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.network_panel_title), style = MaterialTheme.typography.headlineMedium)
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_close))
                    }
                }
            }
            PanelTabs(tab, { tab = it; search = "" })
            OutlinedTextField(
                search,
                { search = it },
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text(stringResource(if (tab == 0) R.string.peers_hint_search_peers else R.string.networks_hint_search_networks)) },
                singleLine = true,
            )
            if (tab == 0) {
                PeerList(
                    peers.filter { it.fqdn.contains(search, true) || it.ip.contains(search, true) },
                    Modifier.weight(1f),
                )
            } else {
                ResourceList(
                    resources.filter { it.name.contains(search, true) || it.address.contains(search, true) },
                    onRouteSelection,
                    Modifier.weight(1f),
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun PanelTabs(selected: Int, onSelected: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(R.string.peers_title, R.string.networks_title).forEachIndexed { index, label ->
            val active = selected == index
            Surface(
                modifier = Modifier.weight(1f).clickable(role = Role.Tab) { onSelected(index) },
                shape = RoundedCornerShape(16.dp),
                color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant),
            ) {
                Text(
                    stringResource(label),
                    modifier = Modifier.padding(vertical = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun PeerList(peers: List<Peer>, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxWidth().testTag("network_list"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(peers, key = { it.fqdn }) { peer ->
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(10.dp).size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Text(peer.fqdn, modifier = Modifier.weight(1f).padding(end = 8.dp), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            StatusBadge(peer.status)
                        }
                        Spacer(Modifier.height(10.dp))
                        AddressLine("IPv4", peer.ip)
                        if (peer.ipv6.isNotBlank()) AddressLine("IPv6", peer.ipv6)
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun ResourceList(resources: List<Resource>, onRouteSelection: (Resource, Boolean) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxWidth().testTag("network_list"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(resources, key = { it.name + it.address }) { resource ->
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.padding(10.dp).size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(resource.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            if (resource.isExitNode) StatusPill(stringResource(R.string.networks_desc_exit_node), MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(7.dp))
                        Text(resource.address, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (resource.peer.isNotBlank()) Text(resource.peer, modifier = Modifier.padding(top = 3.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
private fun AddressLine(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(38.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@androidx.compose.runtime.Composable
private fun StatusBadge(status: Status) = StatusPill(statusLabel(status), statusColor(status))

@androidx.compose.runtime.Composable
private fun StatusPill(label: String, color: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.12f)) {
        Text(label, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = color, maxLines = 1)
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
