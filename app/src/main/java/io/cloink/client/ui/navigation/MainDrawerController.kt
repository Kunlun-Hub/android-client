package io.cloink.client.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.cloink.client.R
import io.cloink.client.ui.theme.CloinkTheme
import java.util.function.IntConsumer

class MainDrawerController(
    private val onDestination: IntConsumer,
    private val onDocs: Runnable,
    private val version: String,
) {
    private var selected by mutableIntStateOf(R.id.nav_home)
    private var profileName by mutableStateOf("default")

    fun install(view: ComposeView) {
        view.setContent {
            CloinkTheme {
                MainDrawer(selected, profileName, version, onDestination::accept, onDocs::run)
            }
        }
    }

    fun select(destination: Int) {
        selected = destination
    }

    fun updateProfile(name: String) {
        profileName = name
    }
}

@androidx.compose.runtime.Composable
internal fun MainDrawer(
    selected: Int,
    profileName: String,
    version: String,
    onDestination: (Int) -> Unit,
    onDocs: () -> Unit,
) {
    Column(
        Modifier
            .width(288.dp)
            .fillMaxHeight()
            .padding(horizontal = 16.dp, vertical = 28.dp),
    ) {
        Text("Cloink", style = MaterialTheme.typography.headlineMedium)
        Text("Secure private networking", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.padding(14.dp))
        DrawerDestination(R.id.nav_home, "Home", selected, onDestination)
        DrawerDestination(R.id.nav_profiles, profileName, selected, onDestination)
        HorizontalDivider(Modifier.padding(vertical = 10.dp))
        DrawerDestination(R.id.nav_advanced, "Advanced", selected, onDestination)
        DrawerDestination(R.id.nav_change_server, "Change server", selected, onDestination)
        DrawerDestination(R.id.nav_troubleshoot, "Troubleshoot", selected, onDestination)
        DrawerDestination(R.id.nav_about, "About", selected, onDestination)
        Text(
            "Documentation",
            modifier = Modifier.clickable(onClick = onDocs).padding(16.dp),
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.weight(1f))
        Text(
            "${stringResource(R.string.about_version)}$version",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@androidx.compose.runtime.Composable
private fun DrawerDestination(id: Int, label: String, selected: Int, onDestination: (Int) -> Unit) {
        NavigationDrawerItem(
            label = { Text(label) },
            selected = selected == id,
            onClick = { onDestination(id) },
        )
}
