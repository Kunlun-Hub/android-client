package io.cloink.client.ui.navigation

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.unit.dp
import io.cloink.client.R
import io.cloink.client.ThemePreferences
import io.cloink.client.ui.theme.CloinkTheme
import java.util.function.IntConsumer
import androidx.compose.material3.Switch
import androidx.compose.ui.semantics.Role

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
                MainDrawer(
                    selected,
                    profileName,
                    version,
                    onDestination::accept,
                    onDocs::run,
                ) { dark ->
                    val mode = if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                    if (ThemePreferences.saveThemeMode(view.context, mode)) {
                        view.post {
                            if (AppCompatDelegate.getDefaultNightMode() != mode) {
                                AppCompatDelegate.setDefaultNightMode(mode)
                            }
                        }
                    }
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
}

@androidx.compose.runtime.Composable
internal fun MainDrawer(
    selected: Int,
    profileName: String,
    version: String,
    onDestination: (Int) -> Unit,
    onDocs: () -> Unit,
    onThemeToggle: (Boolean) -> Unit = {},
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (ThemePreferences.getThemeMode(androidx.compose.ui.platform.LocalContext.current)) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> systemDark
    }
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
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp),
        ) {
            Surface(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 18.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            ) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                    Text("CLOINK", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(3.dp))
                    Text(stringResource(R.string.drawer_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            DrawerDestination(R.id.nav_home, stringResource(R.string.menu_home), Icons.Default.Home, selected, onDestination)
            DrawerDestination(R.id.nav_profiles, profileName, Icons.Default.Person, selected, onDestination)
            HorizontalDivider(Modifier.padding(horizontal = 12.dp, vertical = 12.dp))
            DrawerDestination(R.id.nav_advanced, stringResource(R.string.menu_advanced), Icons.Default.Settings, selected, onDestination)
            DrawerDestination(R.id.nav_change_server, stringResource(R.string.menu_change_server), Icons.Default.Place, selected, onDestination)
            DrawerDestination(R.id.nav_troubleshoot, stringResource(R.string.menu_troubleshoot), Icons.Default.Warning, selected, onDestination)
            DrawerDestination(R.id.nav_about, stringResource(R.string.menu_about), Icons.Default.Info, selected, onDestination)
            DrawerCommand(stringResource(R.string.menu_docs), Icons.AutoMirrored.Filled.List, onDocs)

            Spacer(Modifier.weight(1f))
            HorizontalDivider(Modifier.padding(horizontal = 12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = darkTheme,
                        role = Role.Switch,
                        onValueChange = onThemeToggle,
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(text = stringResource(R.string.drawer_dark_theme), modifier = Modifier.padding(start = 14.dp), style = MaterialTheme.typography.bodyLarge)
                }
                Switch(checked = darkTheme, onCheckedChange = null)
            }
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
        shape = RoundedCornerShape(16.dp),
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
