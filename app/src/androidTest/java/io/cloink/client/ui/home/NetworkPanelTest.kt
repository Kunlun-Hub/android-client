package io.cloink.client.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.cloink.client.ui.theme.CloinkTheme
import java.util.Collections
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NetworkPanelTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun filtersPeersByNameAndAddress() {
        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                NetworkPanel(
                    peers = listOf(
                        Peer(Status.CONNECTED, "100.64.0.2", "", "alpha.cloink"),
                        Peer(Status.IDLE, "100.64.0.3", "", "beta.cloink"),
                    ),
                    resources = emptyList(),
                    onRouteSelection = { _, _ -> },
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("100.64.0.3")
        composeRule.onNodeWithText("beta.cloink").assertIsDisplayed()
    }

    @Test
    fun selectingNetworkReportsTheChosenRoute() {
        val route = Resource(Status.CONNECTED, "office", "10.20.0.0/16", "router", false, Collections.emptyList())
        var selected: Pair<Resource, Boolean>? = null
        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                NetworkPanel(emptyList(), listOf(route)) { resource, enabled -> selected = resource to enabled }
            }
        }

        composeRule.onNodeWithText("Networks").performClick()
        composeRule.onNodeWithText("office").assertIsDisplayed()
        composeRule.onAllNodes(isToggleable())[0].performClick()

        composeRule.runOnIdle { assertEquals(route to true, selected) }
    }
}
