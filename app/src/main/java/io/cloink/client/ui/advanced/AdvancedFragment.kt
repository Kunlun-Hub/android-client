package io.cloink.client.ui.advanced

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import io.cloink.client.R
import io.cloink.client.ThemePreferences
import io.cloink.client.tool.Preferences
import io.cloink.client.tool.ProfileManagerWrapper
import io.cloink.client.ui.components.SettingsPage
import io.cloink.client.ui.components.ToggleSetting
import io.cloink.client.ui.theme.CloinkTheme
import io.cloink.gomobile.android.Preferences as GoPreferences

class AdvancedFragment : Fragment() {
    private lateinit var goPreferences: GoPreferences
    private lateinit var localPreferences: Preferences

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        goPreferences = GoPreferences(ProfileManagerWrapper(requireContext()).activeConfigPath)
        localPreferences = Preferences(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { CloinkTheme { AdvancedScreen() } }
        }

    @Composable
    private fun AdvancedScreen() {
        var settings by remember { mutableStateOf(loadSettings()) }
        var key by remember { mutableStateOf(if (settings.hasPSK) "********" else "") }
        var keyError by remember { mutableStateOf(false) }
        var reconnectNotice by remember { mutableStateOf(false) }
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            SettingsPage(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.advanced_title_presharedkey), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.advanced_details), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it; keyError = false },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = keyError,
                    label = { Text(stringResource(R.string.advanced_hint)) },
                )
                Button(onClick = {
                    if (key == "********") return@Button
                    if (key.isNotBlank() && !key.matches(Regex("^[A-Za-z0-9+/=]{32,64}$"))) {
                        keyError = true
                    } else updateGo({ setPreSharedKey(key.trim()) }) {
                        settings = settings.copy(hasPSK = key.isNotBlank())
                        key = if (key.isBlank()) "" else "********"
                        toast(R.string.advanced_presharedkey_saved_success)
                    }
                }) { Text(stringResource(R.string.advanced_save)) }

                SectionTitle(stringResource(R.string.advanced_title_network_security))
                ToggleSetting(stringResource(R.string.advanced_rosenpass), settings.rosenpass, { enabled ->
                    updateGo({ setRosenpassEnabled(enabled); if (!enabled) setRosenpassPermissive(false) }) {
                        settings = settings.copy(rosenpass = enabled, rosenpassPermissive = if (enabled) settings.rosenpassPermissive else false)
                    }
                })
                ToggleSetting(stringResource(R.string.advanced_rosenpass_permissive), settings.rosenpassPermissive, { enabled ->
                    updateGo({ setRosenpassPermissive(enabled) }) { settings = settings.copy(rosenpassPermissive = enabled) }
                }, description = if (settings.rosenpass) null else stringResource(R.string.advanced_rosenpass))
                ToggleSetting(stringResource(R.string.advanced_allow_ssh), settings.allowSSH, { setBoolean({ setServerSSHAllowed(it) }, it) { settings = settings.copy(allowSSH = it) } }, stringResource(R.string.advanced_allow_ssh_desc))
                ToggleSetting(stringResource(R.string.advanced_block_inbound), settings.blockInbound, { setBoolean({ setBlockInbound(it) }, it) { settings = settings.copy(blockInbound = it) } }, stringResource(R.string.advanced_block_inbound_desc))
                ToggleSetting(stringResource(R.string.advanced_disable_client_routes), settings.disableClientRoutes, { setBoolean({ setDisableClientRoutes(it) }, it) { settings = settings.copy(disableClientRoutes = it) } }, stringResource(R.string.advanced_disable_client_routes_desc))
                ToggleSetting(stringResource(R.string.advanced_disable_server_routes), settings.disableServerRoutes, { setBoolean({ setDisableServerRoutes(it) }, it) { settings = settings.copy(disableServerRoutes = it) } }, stringResource(R.string.advanced_disable_server_routes_desc))
                ToggleSetting(stringResource(R.string.advanced_disable_dns), settings.disableDNS, { setBoolean({ setDisableDNS(it) }, it) { settings = settings.copy(disableDNS = it) } }, stringResource(R.string.advanced_disable_dns_desc))
                ToggleSetting(stringResource(R.string.advanced_disable_firewall), settings.disableFirewall, { setBoolean({ setDisableFirewall(it) }, it) { settings = settings.copy(disableFirewall = it) } }, stringResource(R.string.advanced_disable_firewall_desc))
                ToggleSetting(stringResource(R.string.advanced_disable_ipv6), settings.disableIPv6, { setBoolean({ setDisableIPv6(it) }, it) { settings = settings.copy(disableIPv6 = it) } }, stringResource(R.string.advanced_disable_ipv6_desc))
                ToggleSetting(stringResource(R.string.advanced_force_relay_conn), settings.forceRelay, { enabled ->
                    if (enabled) localPreferences.enableForcedRelayConnection() else localPreferences.disableForcedRelayConnection()
                    settings = settings.copy(forceRelay = enabled)
                    reconnectNotice = true
                }, stringResource(R.string.advanced_force_relay_conn_desc))

                SectionTitle(stringResource(R.string.advanced_theme_title))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeOption(R.string.advanced_theme_system, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, settings.theme, ::setTheme)
                    ThemeOption(R.string.advanced_theme_light, AppCompatDelegate.MODE_NIGHT_NO, settings.theme, ::setTheme)
                    ThemeOption(R.string.advanced_theme_dark, AppCompatDelegate.MODE_NIGHT_YES, settings.theme, ::setTheme)
                }
            }
        }
        if (reconnectNotice) AlertDialog(
            onDismissRequest = { reconnectNotice = false },
            text = { Text(stringResource(R.string.reconnectionNeededWarningMessage)) },
            confirmButton = { Button(onClick = { reconnectNotice = false }) { Text(stringResource(android.R.string.ok)) } },
        )
    }

    @Composable
    private fun ThemeOption(label: Int, mode: Int, selected: Int, onSelect: (Int) -> Unit) {
        FilterChip(selected = selected == mode, onClick = { onSelect(mode) }, label = { Text(stringResource(label)) })
    }

    @Composable
    private fun SectionTitle(title: String) {
        Spacer(Modifier.height(24.dp)); HorizontalDivider(); Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(6.dp))
    }

    private fun setBoolean(setter: GoPreferences.(Boolean) -> Unit, value: Boolean, success: () -> Unit) =
        updateGo({ setter(value) }, success)

    private fun updateGo(change: GoPreferences.() -> Unit, success: () -> Unit) {
        runCatching { goPreferences.change(); goPreferences.commit() }
            .onSuccess { success() }
            .onFailure { Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show() }
    }

    private fun setTheme(mode: Int) {
        if (!ThemePreferences.saveThemeMode(requireContext(), mode)) {
            return
        }
        view?.post {
            if (isAdded && AppCompatDelegate.getDefaultNightMode() != mode) {
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }
    }

    private fun toast(message: Int) = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

    private fun loadSettings(): AdvancedState = runCatching {
        AdvancedState(
            hasPSK = goPreferences.preSharedKey.isNotEmpty(),
            rosenpass = goPreferences.rosenpassEnabled,
            rosenpassPermissive = goPreferences.rosenpassPermissive,
            allowSSH = goPreferences.serverSSHAllowed,
            blockInbound = goPreferences.blockInbound,
            disableClientRoutes = goPreferences.disableClientRoutes,
            disableServerRoutes = goPreferences.disableServerRoutes,
            disableDNS = goPreferences.disableDNS,
            disableFirewall = goPreferences.disableFirewall,
            disableIPv6 = goPreferences.disableIPv6,
            forceRelay = localPreferences.isConnectionForceRelayed,
            theme = ThemePreferences.getThemeMode(requireContext()),
        )
    }.getOrElse { AdvancedState() }
}

private data class AdvancedState(
    val hasPSK: Boolean = false,
    val rosenpass: Boolean = false,
    val rosenpassPermissive: Boolean = false,
    val allowSSH: Boolean = false,
    val blockInbound: Boolean = false,
    val disableClientRoutes: Boolean = false,
    val disableServerRoutes: Boolean = false,
    val disableDNS: Boolean = false,
    val disableFirewall: Boolean = false,
    val disableIPv6: Boolean = false,
    val forceRelay: Boolean = true,
    val theme: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
)
