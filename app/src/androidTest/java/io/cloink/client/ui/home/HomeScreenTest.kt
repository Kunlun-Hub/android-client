package io.cloink.client.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.cloink.client.ui.theme.CloinkTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun connectedStateShowsIdentityAndPeerSummary() {
        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                HomeScreen(
                    state = HomeUiState(
                        status = ConnectionStatus.CONNECTED,
                        fqdn = "workstation.cloink.4w.ink",
                        address = "100.64.0.5",
                        connectedPeers = 3,
                        totalPeers = 5,
                    ),
                    isTelevision = false,
                    onToggleConnection = {},
                    onOpenPeers = {},
                )
            }
        }

        composeRule.onNodeWithText("workstation.cloink.4w.ink").assertIsDisplayed()
        composeRule.onNodeWithText("100.64.0.5").assertIsDisplayed()
        composeRule.onNodeWithText("3 of 5 connected").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Connected").assertIsDisplayed()
    }

    @Test
    fun disconnectedActionAndPeerSummaryRemainInteractive() {
        var toggleCount = 0
        var peerCount = 0
        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                HomeScreen(
                    state = HomeUiState(),
                    isTelevision = false,
                    onToggleConnection = { toggleCount++ },
                    onOpenPeers = { peerCount++ },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Connect").performClick()
        composeRule.onNodeWithText("View").performClick()

        composeRule.runOnIdle {
            assertEquals(1, toggleCount)
            assertEquals(1, peerCount)
        }
    }
}
