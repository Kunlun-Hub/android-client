package io.cloink.client.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.shape.RoundedCornerShape
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
    Surface(
        modifier = Modifier
            .width(304.dp)
            .fillMaxHeight()
            .testTag("main_drawer_surface"),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 12.dp,
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 28.dp, bottom = 20.dp),
            ) {
                Text("Cloink", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.drawer_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            DrawerDestination(R.id.nav_home, stringResource(R.string.menu_home), Icons.Default.Home, selected, onDestination)
            DrawerDestination(R.id.nav_profiles, profileName, Icons.Default.Person, selected, onDestination)
            HorizontalDivider(Modifier.padding(horizontal = 12.dp, vertical = 12.dp))
            DrawerDestination(R.id.nav_advanced, stringResource(R.string.menu_advanced), Icons.Default.Settings, selected, onDestination)
            DrawerDestination(R.id.nav_change_server, stringResource(R.string.menu_change_server), Icons.Default.Place, selected, onDestination)
            DrawerDestination(R.id.nav_troubleshoot, stringResource(R.string.menu_troubleshoot), Icons.Default.Warning, selected, onDestination)
            DrawerDestination(R.id.nav_about, stringResource(R.string.menu_about), Icons.Default.Info, selected, onDestination)
            DrawerCommand(stringResource(R.string.menu_docs), Icons.Default.List, onDocs)

            Spacer(Modifier.weight(1f))
            HorizontalDivider(Modifier.padding(horizontal = 12.dp))
            Text(
                "${stringResource(R.string.about_version)}$version",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 18.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun DrawerDestination(
    id: Int,
    label: String,
    icon: ImageVector,
    selected: Int,
    onDestination: (Int) -> Unit,
) {
    DrawerItem(label, icon, selected == id) { onDestination(id) }
}

@androidx.compose.runtime.Composable
private fun DrawerCommand(label: String, icon: ImageVector, onClick: () -> Unit) {
    DrawerItem(label, icon, selected = false, onClick = onClick)
}

@androidx.compose.runtime.Composable
private fun DrawerItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label, style = MaterialTheme.typography.bodyLarge) },
        icon = { Icon(icon, contentDescription = null) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}
