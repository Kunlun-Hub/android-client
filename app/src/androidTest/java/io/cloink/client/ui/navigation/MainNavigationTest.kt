package io.cloink.client.ui.navigation

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.unit.dp
import io.cloink.client.R
import io.cloink.client.ui.theme.CloinkTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.Locale

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

        val drawer = composeRule.onNodeWithTag("main_drawer_surface")
        drawer.assertWidthIsEqualTo(304.dp)
        val pixels = drawer.captureToImage().toPixelMap()
        val corners = listOf(
            pixels[0, 0],
            pixels[pixels.width - 1, 0],
            pixels[0, pixels.height - 1],
            pixels[pixels.width - 1, pixels.height - 1],
        )
        composeRule.runOnIdle {
            corners.forEach { color ->
                org.junit.Assert.assertEquals(1f, color.alpha, 0.001f)
            }
        }
    }

    @Test
    fun drawerUsesChineseResources() {
        val baseContext = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val localizedContext = baseContext.createConfigurationContext(
            Configuration(baseContext.resources.configuration).apply { setLocale(Locale.SIMPLIFIED_CHINESE) },
        )
        composeRule.setContent {
            CompositionLocalProvider(LocalContext provides localizedContext) {
                CloinkTheme(darkTheme = false) {
                    MainDrawer(R.id.nav_home, "工作", "test", {}, {})
                }
            }
        }

        composeRule.onNodeWithText("首页").assertIsDisplayed()
        composeRule.onNodeWithText("高级设置").assertIsDisplayed()
        composeRule.onNodeWithText("文档").assertIsDisplayed()
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
