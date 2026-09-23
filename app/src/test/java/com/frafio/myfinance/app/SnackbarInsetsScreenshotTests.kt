package com.frafio.myfinance.app

import android.graphics.Rect
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.foundation.layout.windowInsetsStartWidth
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.window.core.layout.WindowSizeClass
import com.frafio.myfinance.core.navigation.HomeTabKey
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.features.home.HomeScreenContent
import com.frafio.myfinance.testing.screenshot.DeviceSpec
import com.frafio.myfinance.testing.screenshot.WindowInsets
import com.frafio.myfinance.testing.screenshot.captureAfter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

/**
 * The same placements with a status bar and navigation bars faked in, so a wrong inset inside the
 * nested scaffolds shows up. The magenta bands mark where `safeDrawing` says the insets are.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class SnackbarInsetsScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val density = 2.625f // 420 dpi, the density every golden is recorded at

    private fun dp(value: Int) = (value * density).toInt()

    private val insets = WindowInsetsCompat.Builder()
        .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(Rect(0, dp(64), 0, 0)))
        .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(Rect(dp(64), 0, dp(64), dp(64))))
        .build()

    private fun snackbar(name: String, widthDp: Int, heightDp: Int, showSnackbar: Boolean = true) {
        lateinit var appState: MyFinanceAppState
        composeTestRule.captureAfter(
            device = DeviceSpec(widthDp, heightDp),
            screenshotName = name,
            deviceName = "shell",
            // The inset override wraps the content in an AndroidView, so the root is not the content.
            capture = { composeTestRule.onNodeWithTag("insets_root") },
            action = {
                if (showSnackbar) {
                    appState.snackbarHostState.showSnackbar(
                        message = "Expense deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Indefinite,
                    )
                }
            },
        ) {
            Box(Modifier.fillMaxSize().testTag("insets_root")) {
                DeviceConfigurationOverride(DeviceConfigurationOverride.WindowInsets(insets)) {
                    appState = rememberMyFinanceAppState()
                    HomeScreenContent(
                        appState = appState,
                        currentTab = HomeTabKey.Expenses,
                        profilePicture = null,
                        proPicChoice = null,
                        windowAdaptiveInfo = WindowAdaptiveInfo(WindowSizeClass(widthDp, heightDp), Posture()),
                        onTabClick = {},
                        onAddClick = {},
                        onLogoutClick = {},
                        onProPicClick = {},
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Screen content") }
                    }
                    DebugVisibleWindowInsets()
                }
            }
        }
    }

    /** Magenta bands over the `safeDrawing` insets, as nowinandroid's insets goldens do. */
    @Composable
    private fun DebugVisibleWindowInsets(color: Color = Color.Magenta.copy(alpha = 0.5f)) {
        Box(Modifier.fillMaxSize()) {
            Spacer(Modifier.align(Alignment.TopCenter).fillMaxWidth().windowInsetsTopHeight(androidx.compose.foundation.layout.WindowInsets.safeDrawing).background(color))
            Spacer(Modifier.align(Alignment.CenterStart).fillMaxHeight().windowInsetsStartWidth(androidx.compose.foundation.layout.WindowInsets.safeDrawing).background(color))
            Spacer(Modifier.align(Alignment.CenterEnd).fillMaxHeight().windowInsetsEndWidth(androidx.compose.foundation.layout.WindowInsets.safeDrawing).background(color))
            Spacer(Modifier.align(Alignment.BottomCenter).fillMaxWidth().windowInsetsBottomHeight(androidx.compose.foundation.layout.WindowInsets.safeDrawing).background(color))
        }
    }

    @Test
    fun insets_compactWidth() = snackbar("insets_snackbar_compact", 411, 891)

    @Test
    fun insets_compactWidth_withoutSnackbar() = snackbar("insets_snackbar_compact_noSnackbar", 411, 891, showSnackbar = false)

    @Test
    fun insets_mediumWidth() = snackbar("insets_snackbar_medium", 700, 891)

    @Test
    fun insets_expandedWidth() = snackbar("insets_snackbar_expanded", 1280, 800)
}
