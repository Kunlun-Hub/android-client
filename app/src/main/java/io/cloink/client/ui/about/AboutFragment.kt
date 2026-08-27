package io.cloink.client.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import io.cloink.client.R
import io.cloink.client.ui.theme.CloinkTheme

class AboutFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CloinkTheme {
                    AboutScreen(
                        version = appVersion(),
                        onLicense = { open("https://github.com/Kunlun-Hub/android-client/blob/main/LICENSE") },
                        onPrivacy = { open("https://cloink.4w.ink") },
                    )
                }
            }
        }

    private fun appVersion(): String = runCatching {
        requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
    }.getOrNull() ?: "unknown"

    private fun open(url: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

@Composable
private fun AboutScreen(version: String, onLicense: () -> Unit, onPrivacy: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Cloink", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("${stringResource(R.string.about_version)}$version", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp))
            Text(stringResource(R.string.about_license), color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onLicense))
            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.about_privacy_poliy), color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onPrivacy))
            Spacer(Modifier.height(48.dp))
            Text(stringResource(R.string.about_rights), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
