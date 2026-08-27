package io.cloink.client.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.cloink.client.tool.Profile
import io.cloink.client.ui.theme.CloinkTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfilesScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun addProfileRequiresANameAndReportsTrimmedValue() {
        var addedName: String? = null
        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                ProfilesScreen(listOf(Profile("default", "Default", true))) { action, name ->
                    if (action == ProfileDialog.Add) addedName = name
                    true
                }
            }
        }

        composeRule.onNodeWithContentDescription("Add Profile").performClick()
        composeRule.onNodeWithText("New Profile").assertIsDisplayed()
        composeRule.onNodeWithText("OK").assertIsNotEnabled()
        composeRule.onNodeWithText("Profile name").performTextInput("  Work  ")
        composeRule.onNodeWithText("OK").performClick()

        composeRule.runOnIdle { assertEquals("Work", addedName) }
    }

    @Test
    fun inactiveNonDefaultProfileExposesAllActions() {
        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                ProfilesScreen(listOf(Profile("work", "Work", false))) { _, _ -> true }
            }
        }

        composeRule.onNodeWithText("Switch").assertIsDisplayed()
        composeRule.onNodeWithText("Logout").assertIsDisplayed()
        composeRule.onNodeWithText("Remove").assertIsDisplayed()
    }
}
