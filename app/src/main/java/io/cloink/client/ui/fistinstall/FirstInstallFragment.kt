package io.cloink.client.ui.fistinstall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import io.cloink.client.PlatformUtils
import io.cloink.client.R
import io.cloink.client.ui.server.ChangeServerFragment
import io.cloink.client.ui.theme.CloinkTheme

class FirstInstallFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CloinkTheme {
                    FirstInstallScreen(
                        showTelevisionNotice = PlatformUtils.isAndroidTV(requireContext()),
                        onContinue = { findNavController().popBackStack() },
                        onChangeServer = ::openChangeServer,
                    )
                }
            }
        }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        super.onStop()
    }

    private fun openChangeServer() {
        val args = Bundle().apply { putBoolean(ChangeServerFragment.HideAlertBundleArg, true) }
        findNavController().navigate(
            R.id.nav_change_server,
            args,
            navOptions { popUpTo(R.id.firstInstallFragment) { inclusive = true } },
        )
    }
}

@Composable
private fun FirstInstallScreen(showTelevisionNotice: Boolean, onContinue: () -> Unit, onChangeServer: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(32.dp).widthIn(max = 440.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Cloink", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.padding(10.dp))
            Text(stringResource(R.string.fragment_firstinstall_txt), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (showTelevisionNotice) {
                Spacer(Modifier.padding(8.dp))
                Text(stringResource(R.string.fragment_firstinstall_androidtv_beta), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.padding(12.dp))
            Button(onClick = onContinue, modifier = Modifier.widthIn(min = 220.dp)) { Text(stringResource(R.string.fragment_firstinstall_continue)) }
            OutlinedButton(onClick = onChangeServer, modifier = Modifier.widthIn(min = 220.dp)) { Text(stringResource(R.string.menu_change_server)) }
        }
    }
}
