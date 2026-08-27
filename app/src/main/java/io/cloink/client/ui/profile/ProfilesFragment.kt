package io.cloink.client.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import io.cloink.client.R
import io.cloink.client.tool.Profile
import io.cloink.client.tool.ProfileManagerWrapper
import io.cloink.client.ui.theme.CloinkTheme

class ProfilesFragment : Fragment() {
    private lateinit var manager: ProfileManagerWrapper

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        manager = ProfileManagerWrapper(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                var profiles by remember { mutableStateOf(manager.listProfiles()) }
                CloinkTheme {
                    ProfilesScreen(profiles) { current, name ->
                        runCatching {
                            when (current) {
                                ProfileDialog.Add -> manager.addProfile(name.orEmpty())
                                is ProfileDialog.Switch -> manager.switchProfile(current.profile.id)
                                is ProfileDialog.Logout -> manager.logoutProfile(current.profile.id)
                                is ProfileDialog.Remove -> {
                                    require(current.profile.id != "default") { getString(R.string.profiles_error_cannot_remove_default) }
                                    require(!current.profile.isActive) { getString(R.string.profiles_error_cannot_remove_active) }
                                    manager.removeProfile(current.profile.id)
                                }
                            }
                        }.onSuccess {
                            profiles = manager.listProfiles()
                            if (current is ProfileDialog.Switch) requireActivity().onBackPressedDispatcher.onBackPressed()
                        }.onFailure { Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show() }
                            .isSuccess
                    }
                }
            }
        }
}

internal sealed interface ProfileDialog {
    data object Add : ProfileDialog
    data class Switch(val profile: Profile) : ProfileDialog
    data class Logout(val profile: Profile) : ProfileDialog
    data class Remove(val profile: Profile) : ProfileDialog
}

@androidx.compose.runtime.Composable
internal fun ProfilesScreen(profiles: List<Profile>, onAction: (ProfileDialog, String?) -> Boolean) {
    var dialog by remember { mutableStateOf<ProfileDialog?>(null) }
    val addProfileDescription = stringResource(R.string.profiles_add)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { dialog = ProfileDialog.Add },
                modifier = Modifier.semantics { contentDescription = addProfileDescription },
            ) { Icon(Icons.Default.Add, contentDescription = null) }
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(profiles, key = { it.id }) { profile ->
                ProfileCard(
                    profile,
                    onSwitch = { dialog = ProfileDialog.Switch(profile) },
                    onLogout = { dialog = ProfileDialog.Logout(profile) },
                    onRemove = { dialog = ProfileDialog.Remove(profile) },
                )
            }
        }
    }
    dialog?.let { current ->
        ProfileActionDialog(current, onDismiss = { dialog = null }) { name ->
            if (onAction(current, name)) dialog = null
        }
    }
}

@androidx.compose.runtime.Composable
private fun ProfileCard(profile: Profile, onSwitch: () -> Unit, onLogout: () -> Unit, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(profile.name, style = MaterialTheme.typography.titleMedium)
                if (profile.isActive) Text(stringResource(R.string.profiles_active), color = MaterialTheme.colorScheme.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!profile.isActive) OutlinedButton(onClick = onSwitch) { Text(stringResource(R.string.profiles_switch)) }
                OutlinedButton(onClick = onLogout) { Text(stringResource(R.string.profiles_logout)) }
                if (profile.id != "default" && !profile.isActive) OutlinedButton(onClick = onRemove) { Text(stringResource(R.string.profiles_remove)) }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun ProfileActionDialog(dialog: ProfileDialog, onDismiss: () -> Unit, onConfirm: (String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    val profile = when (dialog) { is ProfileDialog.Switch -> dialog.profile; is ProfileDialog.Logout -> dialog.profile; is ProfileDialog.Remove -> dialog.profile; else -> null }
    val title = when (dialog) { ProfileDialog.Add -> R.string.profiles_dialog_add_title; is ProfileDialog.Switch -> R.string.profiles_dialog_switch_title; is ProfileDialog.Logout -> R.string.profiles_dialog_logout_title; is ProfileDialog.Remove -> R.string.profiles_dialog_remove_title }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(title)) }, text = {
        if (dialog == ProfileDialog.Add) OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.profiles_dialog_add_hint)) })
        else Text(when (dialog) {
            is ProfileDialog.Switch -> stringResource(R.string.profiles_dialog_switch_message, profile!!.name)
            is ProfileDialog.Logout -> stringResource(R.string.profiles_dialog_logout_message, profile!!.name)
            is ProfileDialog.Remove -> stringResource(R.string.profiles_dialog_remove_message, profile!!.name)
            else -> ""
        })
    }, confirmButton = { Button(enabled = dialog != ProfileDialog.Add || name.isNotBlank(), onClick = { onConfirm(if (dialog == ProfileDialog.Add) name.trim() else null) }) { Text(androidx.compose.ui.res.stringResource(android.R.string.ok)) } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text(androidx.compose.ui.res.stringResource(android.R.string.cancel)) } })
}
