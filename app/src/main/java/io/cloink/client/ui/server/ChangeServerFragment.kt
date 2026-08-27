package io.cloink.client.ui.server

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.cloink.client.R
import io.cloink.client.ServiceAccessor
import io.cloink.client.tool.Preferences
import io.cloink.client.tool.ProfileManagerWrapper
import io.cloink.client.ui.components.SettingsPage
import io.cloink.client.ui.theme.CloinkTheme

class ChangeServerFragment : Fragment() {
    companion object { const val HideAlertBundleArg = "hideAlert" }
    private lateinit var serviceAccessor: ServiceAccessor
    private lateinit var viewModel: ChangeServerFragmentViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        serviceAccessor = context as? ServiceAccessor ?: error("$context must implement ServiceAccessor")
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val path = ProfileManagerWrapper(requireContext()).activeConfigPath
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(type: Class<T>): T =
                ChangeServerFragmentViewModel(path, Build.MODEL) { serviceAccessor.stopEngine() } as T
        }
        viewModel = ViewModelProvider(this, factory)[ChangeServerFragmentViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val ui by viewModel.uiState.observeAsState(ChangeServerFragmentUiState.Builder().build())
                var server by remember { mutableStateOf("") }
                var setupKey by remember { mutableStateOf("") }
                var showKey by remember { mutableStateOf(false) }
                var warning by remember { mutableStateOf(ui.shouldDisplayWarningDialog && arguments?.getBoolean(HideAlertBundleArg) != true) }
                CloinkTheme {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        SettingsPage {
                            OutlinedTextField(server, { server = it }, Modifier.fillMaxWidth(), enabled = ui.isUiEnabled,
                                label = { Text(stringResource(R.string.change_server_title_server)) },
                                placeholder = { Text(stringResource(R.string.change_server_hint)) },
                                isError = ui.isUrlInvalid || !ui.errorMessage.isNullOrBlank(),
                                supportingText = { ui.errorMessage?.let { Text(it) } })
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(onClick = { showKey = !showKey }, enabled = ui.isUiEnabled) { Text(stringResource(R.string.change_server_setup_key)) }
                            if (showKey) OutlinedTextField(setupKey, { setupKey = it }, Modifier.fillMaxWidth(), enabled = ui.isUiEnabled,
                                visualTransformation = PasswordVisualTransformation(), isError = ui.isSetupKeyInvalid,
                                label = { Text(stringResource(R.string.change_server_setup_key)) },
                                supportingText = { Text(stringResource(if (ui.isSetupKeyInvalid) R.string.change_server_error_invalid_setup_key else R.string.change_server_setup_key_warning)) })
                            Spacer(Modifier.height(20.dp))
                            Button(enabled = ui.isUiEnabled && (server.isNotBlank() || setupKey.isNotBlank()), onClick = {
                                val target = server.trim().ifBlank { Preferences.defaultServer() }
                                if (setupKey.isBlank()) viewModel.changeManagementServerAddress(target) else viewModel.loginWithSetupKey(target, setupKey.trim())
                            }) { Text(stringResource(if (ui.isUiEnabled) R.string.change_server_btn else R.string.change_server_verifying)) }
                            OutlinedButton(enabled = ui.isUiEnabled, onClick = { server = Preferences.defaultServer() }) { Text(stringResource(R.string.change_server_btn_reset)) }
                        }
                    }
                    if (warning) AlertDialog(onDismissRequest = {}, title = { Text(stringResource(R.string.change_server_alert_title)) },
                        text = { Text(stringResource(R.string.change_server_alert_desc)) },
                        confirmButton = { Button(onClick = { warning = false }) { Text(stringResource(R.string.change_server_alert_yes)) } },
                        dismissButton = { OutlinedButton(onClick = { findNavController().popBackStack() }) { Text(stringResource(R.string.change_server_alert_cancel)) } })
                    if (ui.isOperationSuccessful) AlertDialog(onDismissRequest = { findNavController().popBackStack() },
                        title = { Text(stringResource(R.string.change_server_success_title)) }, text = { Text(stringResource(R.string.change_server_success_desc)) },
                        confirmButton = { Button(onClick = { findNavController().popBackStack() }) { Text(stringResource(R.string.change_server_success_cloe)) } })
                }
            }
        }
}
