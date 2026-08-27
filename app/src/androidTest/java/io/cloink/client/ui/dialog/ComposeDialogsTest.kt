package io.cloink.client.ui.dialog

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import io.cloink.client.ui.theme.CloinkTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ComposeDialogsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun alwaysOnDialogRendersAndCloses() {
        var closed = false
        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                AlwaysOnDialogContent(
                    "Disable <b>Always On</b> for the other VPN application.",
                    onClose = { closed = true },
                )
            }
        }

        composeRule.onNodeWithText("VPN settings required").assertIsDisplayed()
        assertVisuallyNonBlank(composeRule.onRoot().captureToImage())
        composeRule.onNodeWithText("Close").performClick()
        composeRule.runOnIdle { assertTrue(closed) }
    }

    @Test
    fun updateDialogReportsDownloadAction() {
        var downloaded = false
        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                UpdateDialogContent(
                    message = "Version 0.78.0 is available.",
                    onLater = {},
                    onDownload = { downloaded = true },
                )
            }
        }

        composeRule.onNodeWithText("Cloink update available").assertIsDisplayed()
        composeRule.onNodeWithText("Download").performClick()
        composeRule.runOnIdle { assertTrue(downloaded) }
    }

    private fun assertVisuallyNonBlank(image: ImageBitmap) {
        val pixels = image.toPixelMap()
        val colors = HashSet<ULong>()
        for (x in 0 until pixels.width step 8) {
            for (y in 0 until pixels.height step 8) {
                colors += pixels[x, y].value
                if (colors.size >= 4) return
            }
        }
        assertTrue("Rendered dialog should contain multiple visible colors", colors.size >= 4)
    }
}
