package io.cloink.client.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
                MainDrawer(R.id.nav_home, "Work profile", { destination = it }, { docsOpened = true })
            }
        }

        composeRule.onNodeWithText("Advanced").performClick()
        composeRule.runOnIdle { assertEquals(R.id.nav_advanced, destination) }
        composeRule.onNodeWithText("Documentation").performClick()
        composeRule.runOnIdle { assertEquals(true, docsOpened) }
    }

    @Test
    fun topBarUsesMenuOnHomeAndBackElsewhere() {
        var menuCount = 0
        var backCount = 0
        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                MainTopBar(R.id.nav_home, "", { menuCount++ }, { backCount++ })
            }
        }
        composeRule.onNodeWithText("Menu").performClick()

        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                MainTopBar(R.id.nav_about, "About", { menuCount++ }, { backCount++ })
            }
        }
        composeRule.onNodeWithText("About").assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()

        composeRule.runOnIdle {
            assertEquals(1, menuCount)
            assertEquals(1, backCount)
        }
    }
}
