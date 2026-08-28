package io.cloink.client.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.cloink.client.R
import io.cloink.client.ui.theme.CloinkTheme

@Composable
fun HomeScreen(
    state: HomeUiState,
    isTelevision: Boolean,
    onToggleConnection: () -> Unit,
    onOpenPeers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    if (isTelevision && state.actionEnabled) {
        LaunchedEffect(state.actionEnabled) { focusRequester.requestFocus() }
    }

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DeviceIdentity(state.fqdn, state.address, Modifier.fillMaxWidth().padding(top = 18.dp))
            Spacer(Modifier.weight(0.7f))
            Text(
                "CLOINK",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
            )
            Spacer(Modifier.height(24.dp))
            ConnectionControl(
                state,
                onToggleConnection,
                Modifier.focusRequester(focusRequester).focusable(enabled = state.actionEnabled),
            )
            Spacer(Modifier.height(18.dp))
            Text(statusLabel(state.status), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(
                statusDescription(state.status),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.weight(1f))
            PeerSummary(state.connectedPeers, state.totalPeers, onOpenPeers)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DeviceIdentity(fqdn: String, address: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.widthIn(max = 640.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(9.dp).size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    fqdn.ifBlank { stringResource(R.string.home_device_unavailable) },
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (address.isNotBlank()) {
                    Text(address, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun ConnectionControl(state: HomeUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val connected = state.status == ConnectionStatus.CONNECTED
    val working = state.status == ConnectionStatus.CONNECTING || state.status == ConnectionStatus.DISCONNECTING
    val trackColor by animateColorAsState(
        if (connected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
        label = "connection track",
    )
    val scale by animateFloatAsState(if (working) 0.98f else 1f, label = "connection scale")
    val label = when (state.status) {
        ConnectionStatus.DISCONNECTED -> stringResource(R.string.home_connect)
        ConnectionStatus.CONNECTING -> stringResource(R.string.home_connecting)
        ConnectionStatus.CONNECTED -> stringResource(R.string.home_connected)
        ConnectionStatus.DISCONNECTING -> stringResource(R.string.home_disconnecting)
    }

    Surface(
        modifier = modifier
            .width(156.dp)
            .height(88.dp)
            .scale(scale)
            .semantics { role = Role.Switch; contentDescription = label }
            .clickable(enabled = state.actionEnabled, role = Role.Switch, onClick = onClick),
        shape = RoundedCornerShape(44.dp),
        color = trackColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = if (connected) 6.dp else 0.dp,
    ) {
        Box(
            Modifier.fillMaxSize().padding(horizontal = 10.dp),
            contentAlignment = if (connected) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp) {
                Box(Modifier.size(68.dp), contentAlignment = Alignment.Center) {
                    if (working) {
                        CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
                    } else {
                        if (connected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun statusLabel(status: ConnectionStatus): String = when (status) {
    ConnectionStatus.DISCONNECTED -> stringResource(R.string.main_status_disconnected)
    ConnectionStatus.CONNECTING -> stringResource(R.string.home_connecting)
    ConnectionStatus.CONNECTED -> stringResource(R.string.home_connected)
    ConnectionStatus.DISCONNECTING -> stringResource(R.string.home_disconnecting)
}

@Composable
private fun statusDescription(status: ConnectionStatus): String = when (status) {
    ConnectionStatus.DISCONNECTED -> stringResource(R.string.home_status_disconnected)
    ConnectionStatus.CONNECTING -> stringResource(R.string.home_status_connecting)
    ConnectionStatus.CONNECTED -> stringResource(R.string.home_status_connected)
    ConnectionStatus.DISCONNECTING -> stringResource(R.string.home_status_disconnecting)
}

@Composable
private fun PeerSummary(connected: Int, total: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().widthIn(max = 640.dp).clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 17.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(stringResource(R.string.home_network_peers), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.home_peers_connected, connected, total),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.home_view_peers), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 760)
@Composable
private fun HomeScreenPreview() {
    CloinkTheme(darkTheme = false) {
        HomeScreen(HomeUiState(ConnectionStatus.CONNECTED, "workstation.cloink.4w.ink", "100.122.205.194", 4, 6), false, {}, {})
    }
}
