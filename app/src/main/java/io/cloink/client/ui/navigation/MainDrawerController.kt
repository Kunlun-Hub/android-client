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
import androidx.compose.ui.unit.dp
import io.cloink.client.R
import io.cloink.client.ui.theme.CloinkTheme

class MainDrawerController(
    private val onDestination: (Int) -> Unit,
    private val onDocs: () -> Unit,
) {
    private var selected by mutableIntStateOf(R.id.nav_home)
    private var profileName by mutableStateOf("default")

    fun install(view: ComposeView) {
        view.setContent {
            CloinkTheme {
                Column(
                    Modifier
                        .width(288.dp)
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp, vertical = 28.dp),
                ) {
                    Text("Cloink", style = MaterialTheme.typography.headlineMedium)
                    Text("Secure private networking", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.padding(14.dp))
                    Destination(R.id.nav_home, "Home")
                    Destination(R.id.nav_profiles, profileName)
                    HorizontalDivider(Modifier.padding(vertical = 10.dp))
                    Destination(R.id.nav_advanced, "Advanced")
                    Destination(R.id.nav_change_server, "Change server")
                    Destination(R.id.nav_troubleshoot, "Troubleshoot")
                    Destination(R.id.nav_about, "About")
                    Text(
                        "Documentation",
                        modifier = Modifier.clickable(onClick = onDocs).padding(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }

    fun select(destination: Int) {
        selected = destination
    }

    fun updateProfile(name: String) {
        profileName = name
    }

    @androidx.compose.runtime.Composable
    private fun Destination(id: Int, label: String) {
        NavigationDrawerItem(
            label = { Text(label) },
            selected = selected == id,
            onClick = { onDestination(id) },
        )
    }
}
