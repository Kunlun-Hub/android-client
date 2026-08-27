package io.cloink.client.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.cloink.client.ui.theme.CloinkTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsComponentsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun toggleSettingDisplaysContextAndReportsTheNewValue() {
        var requested: Boolean? = null
        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                ToggleSetting(
                    title = "Block inbound connections",
                    checked = false,
                    onCheckedChange = { requested = it },
                    description = "Only established outbound sessions remain available",
                )
            }
        }

        composeRule.onNodeWithText("Only established outbound sessions remain available").assertIsDisplayed()
        composeRule.onNodeWithText("Block inbound connections").performClick()
        composeRule.runOnIdle { assertEquals(true, requested) }
    }
}
