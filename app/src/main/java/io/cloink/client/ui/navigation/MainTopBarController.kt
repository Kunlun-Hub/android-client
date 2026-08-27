package io.cloink.client.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import io.cloink.client.R
import io.cloink.client.ui.theme.CloinkTheme

class MainTopBarController(
    private val onMenu: () -> Unit,
    private val onBack: () -> Unit,
) {
    private val current = mutableStateOf(R.id.nav_home)
    private val title = mutableStateOf("")

    fun install(view: ComposeView) {
        view.setContent {
            CloinkTheme {
                MainTopBar(current.value, title.value, onMenu, onBack)
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
            Box(
                Modifier
                    .clickable { if (destination == R.id.nav_home) onMenu() else onBack() }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Text(if (destination == R.id.nav_home) "Menu" else "Back", color = MaterialTheme.colorScheme.primary)
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
