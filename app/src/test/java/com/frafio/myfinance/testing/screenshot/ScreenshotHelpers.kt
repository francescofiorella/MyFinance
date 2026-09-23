package com.frafio.myfinance.testing.screenshot

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.DarkMode
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziOptions.CompareOptions
import com.github.takahirom.roborazzi.RoborazziOptions.RecordOptions
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.robolectric.RuntimeEnvironment

/** Pixel-perfect comparison; goldens stored at half size. */
val DefaultRoborazziOptions = RoborazziOptions(
    compareOptions = CompareOptions(changeThreshold = 0f),
    recordOptions = RecordOptions(resizeScale = 0.5),
)

data class DeviceSpec(val widthDp: Int, val heightDp: Int, val dpi: Int = 420)

/** Portrait phone first: the app is phone-first, the other two show the navigation rail. */
enum class DefaultTestDevices(val description: String, val spec: DeviceSpec) {
    PHONE("phone", DeviceSpec(411, 891)),
    FOLDABLE("foldable", DeviceSpec(673, 841)),
    TABLET("tablet", DeviceSpec(1280, 800)),
}

private const val SCREENSHOTS_DIR = "src/test/screenshots"

fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.captureMultiDevice(
    screenshotName: String,
    body: @Composable () -> Unit,
) {
    DefaultTestDevices.entries.forEach { captureForDevice(it.spec, screenshotName, deviceName = it.description, body = body) }
}

fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.capturePhoneDark(
    screenshotName: String,
    body: @Composable () -> Unit,
) {
    captureForDevice(DefaultTestDevices.PHONE.spec, screenshotName, deviceName = "phone_dark", darkMode = true, body = body)
}

/**
 * Sets the Robolectric qualifiers to the device, renders [body] under the app theme with
 * [LocalInspectionMode] on (sheets render inline) and captures the root. Dark mode goes through
 * [DeviceConfigurationOverride] so both `MyFinanceTheme`'s default and the components that read
 * `isSystemInDarkTheme()` directly follow it.
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.captureForDevice(
    device: DeviceSpec,
    screenshotName: String,
    deviceName: String,
    darkMode: Boolean = false,
    roborazziOptions: RoborazziOptions = DefaultRoborazziOptions,
    body: @Composable () -> Unit,
) {
    RuntimeEnvironment.setQualifiers("w${device.widthDp}dp-h${device.heightDp}dp-${device.dpi}dpi")

    activity.setContent {
        CompositionLocalProvider(LocalInspectionMode provides true) {
            DeviceConfigurationOverride(DeviceConfigurationOverride.DarkMode(darkMode)) {
                MyFinanceTheme(darkTheme = darkMode, dynamicColor = false) {
                    // The tab contents are transparent; in the app the Home scaffold paints this colour under them.
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        body()
                    }
                }
            }
        }
    }
    waitForIdle()

    onRoot().captureRoboImage("$SCREENSHOTS_DIR/${screenshotName}_$deviceName.png", roborazziOptions = roborazziOptions)
}

/**
 * Four captures of one component: light/dark × dynamic/notDynamic colour, written to
 * `src/test/screenshots/<name>/<file>_<light|dark>_<dynamic|notDynamic>.png`. [content] receives a
 * description of the variant to render into the component, as nowinandroid does.
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.captureMultiTheme(
    name: String,
    overrideFileName: String? = null,
    content: @Composable (description: String) -> Unit,
) {
    var darkMode by mutableStateOf(false)
    var dynamicColor by mutableStateOf(false)

    activity.setContent {
        CompositionLocalProvider(LocalInspectionMode provides true) {
            DeviceConfigurationOverride(DeviceConfigurationOverride.DarkMode(darkMode)) {
                MyFinanceTheme(darkTheme = darkMode, dynamicColor = dynamicColor) {
                    // Keying restarts animations and remembered state per variant.
                    key(darkMode, dynamicColor) {
                        val description = (if (darkMode) "Dark" else "Light") + (if (dynamicColor) " Dynamic" else "")
                        content(description)
                    }
                }
            }
        }
    }

    listOf(false, true).forEach { isDark ->
        listOf(false, true).forEach { isDynamic ->
            darkMode = isDark
            dynamicColor = isDynamic
            waitForIdle()
            val file = overrideFileName ?: name
            val darkDesc = if (isDark) "dark" else "light"
            val dynamicDesc = if (isDynamic) "dynamic" else "notDynamic"
            onRoot().captureRoboImage(
                "$SCREENSHOTS_DIR/$name/${file}_${darkDesc}_$dynamicDesc.png",
                roborazziOptions = DefaultRoborazziOptions,
            )
        }
    }
}

/**
 * Like [captureForDevice], but runs [action] on a composition-scoped coroutine after the content is
 * set and before the capture — the way to show a snackbar in a golden. [capture] names the node to
 * photograph; an inset override wraps the content in an `AndroidView`, so those tests capture a
 * tagged node instead of the root.
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.captureAfter(
    device: DeviceSpec,
    screenshotName: String,
    deviceName: String,
    darkMode: Boolean = false,
    capture: () -> SemanticsNodeInteraction = { onRoot() },
    action: suspend CoroutineScope.() -> Unit = {},
    body: @Composable () -> Unit,
) {
    RuntimeEnvironment.setQualifiers("w${device.widthDp}dp-h${device.heightDp}dp-${device.dpi}dpi")

    lateinit var scope: CoroutineScope
    activity.setContent {
        scope = rememberCoroutineScope()
        CompositionLocalProvider(LocalInspectionMode provides true) {
            DeviceConfigurationOverride(DeviceConfigurationOverride.DarkMode(darkMode)) {
                MyFinanceTheme(darkTheme = darkMode, dynamicColor = false) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        body()
                    }
                }
            }
        }
    }
    scope.launch { action() }
    waitForIdle()

    capture().captureRoboImage("$SCREENSHOTS_DIR/${screenshotName}_$deviceName.png", roborazziOptions = DefaultRoborazziOptions)
}
