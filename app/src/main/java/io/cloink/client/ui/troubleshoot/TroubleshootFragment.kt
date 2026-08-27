package io.cloink.client.ui.troubleshoot

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import io.cloink.client.R
import io.cloink.client.ServiceAccessor
import io.cloink.client.tool.Preferences
import io.cloink.client.ui.components.SettingsPage
import io.cloink.client.ui.components.ToggleSetting
import io.cloink.client.ui.theme.CloinkTheme

class TroubleshootFragment : Fragment() {
    private lateinit var serviceAccessor: ServiceAccessor
    private var generating by mutableStateOf(false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        serviceAccessor = context as? ServiceAccessor ?: error("$context must implement ServiceAccessor")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val preferences = Preferences(requireContext())
                var trace by mutableStateOf(preferences.isTraceLogEnabled)
                var anonymize by mutableStateOf(false)
                CloinkTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        SettingsPage {
                            ToggleSetting(stringResource(R.string.advanced_tracelog), trace, {
                                trace = it
                                if (it) preferences.enableTraceLog() else preferences.disableTraceLog()
                            })
                            ToggleSetting(stringResource(R.string.troubleshoot_anonymize), anonymize, { anonymize = it })
                            Button(enabled = !generating, onClick = { generate(anonymize) }) {
                                if (generating) CircularProgressIndicator() else Text(stringResource(R.string.troubleshoot_debug_bundle))
                            }
                        }
                    }
                }
            }
        }

    private fun generate(anonymize: Boolean) {
        generating = true
        Thread {
            runCatching { serviceAccessor.debugBundle(anonymize) }
                .onSuccess { key -> requireActivity().runOnUiThread { copyResult(key) } }
                .onFailure { error -> requireActivity().runOnUiThread { finishWithError(error) } }
        }.start()
    }

    private fun copyResult(key: String) {
        generating = false
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.troubleshoot_debug_bundle_key), key))
        Toast.makeText(requireContext(), R.string.troubleshoot_debug_bundle_key_copied, Toast.LENGTH_SHORT).show()
    }

    private fun finishWithError(error: Throwable) {
        generating = false
        Toast.makeText(requireContext(), getString(R.string.troubleshoot_debug_bundle_failed, error.message.orEmpty()), Toast.LENGTH_LONG).show()
    }
}
