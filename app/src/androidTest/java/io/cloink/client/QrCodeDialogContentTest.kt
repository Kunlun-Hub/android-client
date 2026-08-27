package io.cloink.client

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.cloink.client.ui.theme.CloinkTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class QrCodeDialogContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersQrCodeAndDeviceCodeAndCloses() {
        var closed = false
        composeRule.setContent {
            CloinkTheme(darkTheme = false) {
                QrCodeDialogContent(
                    url = "https://cloink.4w.ink/device?code=ABCD-EFGH",
                    userCode = "ABCD-EFGH",
                    onClose = { closed = true },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Sign-in QR code").assertIsDisplayed()
        composeRule.onNodeWithText("Device Code: ABCD-EFGH").assertIsDisplayed()
        composeRule.onNodeWithText("Close").performClick()
        composeRule.runOnIdle { assertTrue(closed) }
    }
}
