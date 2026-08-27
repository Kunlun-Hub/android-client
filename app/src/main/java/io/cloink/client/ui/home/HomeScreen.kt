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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        LaunchedEffect(state.actionEnabled) {
            focusRequester.requestFocus()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DeviceIdentity(
                fqdn = state.fqdn,
                address = state.address,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                ConnectionControl(
                    state = state,
                    onClick = onToggleConnection,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .focusable(enabled = state.actionEnabled),
                )
            }

            PeerSummary(
                connected = state.connectedPeers,
                total = state.totalPeers,
                onClick = onOpenPeers,
            )
        }
    }
}

@Composable
private fun DeviceIdentity(
    fqdn: String,
    address: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = fqdn.ifBlank { "Cloink" },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = address.ifBlank { "--" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun ConnectionControl(
    state: HomeUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val connected = state.status == ConnectionStatus.CONNECTED
    val working = state.status == ConnectionStatus.CONNECTING ||
        state.status == ConnectionStatus.DISCONNECTING
    val containerColor by animateColorAsState(
        targetValue = when {
            connected -> MaterialTheme.colorScheme.secondary
            working -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.primary
        },
        label = "connection color",
    )
    val scale by animateFloatAsState(
        targetValue = if (working) 0.96f else 1f,
        label = "connection scale",
    )
    val label = when (state.status) {
        ConnectionStatus.DISCONNECTED -> stringResource(R.string.home_connect)
        ConnectionStatus.CONNECTING -> stringResource(R.string.home_connecting)
        ConnectionStatus.CONNECTED -> stringResource(R.string.home_connected)
        ConnectionStatus.DISCONNECTING -> stringResource(R.string.home_disconnecting)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = modifier
                .size(184.dp)
                .scale(scale)
                .semantics {
                    role = Role.Button
                    contentDescription = label
                }
                .clickable(
                    enabled = state.actionEnabled,
                    role = Role.Button,
                    onClick = onClick,
                ),
            shape = CircleShape,
            color = containerColor,
            contentColor = if (working) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                Color.White
            },
            tonalElevation = if (working) 0.dp else 3.dp,
            shadowElevation = if (working) 0.dp else 5.dp,
            border = if (working) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            } else {
                null
            },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = statusLabel(state.status),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun statusLabel(status: ConnectionStatus): String = when (status) {
    ConnectionStatus.DISCONNECTED -> stringResource(R.string.main_status_disconnected)
    ConnectionStatus.CONNECTING -> stringResource(R.string.home_status_connecting)
    ConnectionStatus.CONNECTED -> stringResource(R.string.home_status_connected)
    ConnectionStatus.DISCONNECTING -> stringResource(R.string.home_status_disconnecting)
}

@Composable
private fun PeerSummary(
    connected: Int,
    total: Int,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_network_peers),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.home_peers_connected, connected, total),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.home_view_peers),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 760)
@Composable
private fun HomeScreenPreview() {
    CloinkTheme(darkTheme = false) {
        HomeScreen(
            state = HomeUiState(
                status = ConnectionStatus.CONNECTED,
                fqdn = "workstation.cloink.4w.ink",
                address = "100.122.205.194",
                connectedPeers = 4,
                totalPeers = 6,
            ),
            isTelevision = false,
            onToggleConnection = {},
            onOpenPeers = {},
        )
    }
}
