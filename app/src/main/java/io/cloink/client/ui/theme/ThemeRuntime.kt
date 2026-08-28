package io.cloink.client.ui.theme

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

object ThemeRuntime {
    var mode by mutableIntStateOf(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        private set

    @JvmStatic
    fun update(mode: Int) {
        this.mode = mode
    }
}
