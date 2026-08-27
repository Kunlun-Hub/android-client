package io.cloink.client.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import io.cloink.client.R
import io.cloink.client.ui.theme.CloinkTheme

class MainTopBarController(
    private val onMenu: Runnable,
    private val onBack: Runnable,
) {
    private val current = mutableStateOf(R.id.nav_home)
    private val title = mutableStateOf("")

    fun install(view: ComposeView) {
        view.setContent {
            CloinkTheme {
                MainTopBar(current.value, title.value, onMenu::run, onBack::run)
            }
        }
    }

    fun update(destination: Int, destinationTitle: String?) {
        current.value = destination
        title.value = destinationTitle.orEmpty()
    }
}

@androidx.compose.runtime.Composable
internal fun MainTopBar(destination: Int, title: String, onMenu: () -> Unit, onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            val home = destination == R.id.nav_home
            IconButton(onClick = { if (home) onMenu() else onBack() }) {
                Icon(
                    imageVector = if (home) Icons.Default.Menu else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(if (home) R.string.navigation_menu else R.string.navigation_back),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
