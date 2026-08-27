package io.cloink.client.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.mutableIntStateOf
import io.cloink.client.R
import io.cloink.client.ui.theme.CloinkTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun drawerReportsDestinationAndDocumentationActions() {
        var destination = 0
        var docsOpened = false
        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                MainDrawer(R.id.nav_home, "Work profile", "test", { destination = it }, { docsOpened = true })
            }
        }

        composeRule.onNodeWithText("Advanced").performClick()
        composeRule.runOnIdle { assertEquals(R.id.nav_advanced, destination) }
        composeRule.onNodeWithText("Docs").performClick()
        composeRule.runOnIdle { assertEquals(true, docsOpened) }
    }

    @Test
    fun topBarUsesMenuOnHomeAndBackElsewhere() {
        var menuCount = 0
        var backCount = 0
        val destination = mutableIntStateOf(R.id.nav_home)
        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                MainTopBar(
                    destination.intValue,
                    if (destination.intValue == R.id.nav_home) "" else "About",
                    { menuCount++ },
                    { backCount++ },
                )
            }
        }
        composeRule.onNodeWithContentDescription("Menu").performClick()

        composeRule.runOnIdle { destination.intValue = R.id.nav_about }
        composeRule.onNodeWithText("About").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back").performClick()

        composeRule.runOnIdle {
            assertEquals(1, menuCount)
            assertEquals(1, backCount)
        }
    }
}
