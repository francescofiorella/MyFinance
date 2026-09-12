package com.frafio.myfinance.baselineProfile

import android.os.SystemClock
import android.util.Log
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

internal const val PACKAGE_NAME = "com.frafio.myfinance"
internal const val TAG = "MyFinanceBP"

private const val IDLE_BUDGET_MS = 600L
private const val FRAME_MS = 120L
private const val FIND_MS = 2_000L
private const val MAX_SCROLL_STEPS = 5

/** Read from the `bpEmail` / `bpPassword` instrumentation arguments; never hardcoded. */
internal object Credentials {
    private val arguments = InstrumentationRegistry.getArguments()

    val email: String get() = arguments.getString("bpEmail").orEmpty()
    val password: String get() = arguments.getString("bpPassword").orEmpty()
    val areAvailable: Boolean get() = email.isNotEmpty() && password.isNotEmpty()

    const val MISSING_MESSAGE: String =
        "No signed-in session and no credentials. Pass them as instrumentation arguments:\n" +
            "  -Pandroid.testInstrumentationRunnerArguments.bpEmail=you@example.com\n" +
            "  -Pandroid.testInstrumentationRunnerArguments.bpPassword=your-password"
}

/** Works only because MainActivity's root Surface sets `testTagsAsResourceId = true`. */
internal fun res(testTag: String): BySelector = By.res(testTag)

internal fun UiDevice.await(testTag: String, timeoutMs: Long): UiObject2? =
    wait(Until.findObject(res(testTag)), timeoutMs)

internal fun UiDevice.awaitTag(testTag: String, timeoutMs: Long): Boolean =
    wait(Until.hasObject(res(testTag)), timeoutMs)

internal fun UiDevice.waitForAny(timeoutMs: Long, vararg testTags: String): Boolean {
    val deadline = SystemClock.uptimeMillis() + timeoutMs
    while (SystemClock.uptimeMillis() < deadline) {
        if (testTags.any { hasObject(res(it)) }) return true
        SystemClock.sleep(100)
    }
    return false
}

/** waitForIdle alone can return mid-animation; the short sleep covers the last frame. */
internal fun UiDevice.settle() {
    waitForIdle(IDLE_BUDGET_MS)
    SystemClock.sleep(FRAME_MS)
}

internal fun UiDevice.tap(testTag: String, timeoutMs: Long = FIND_MS): Boolean {
    val target = await(testTag, timeoutMs) ?: return false
    target.click()
    settle()
    return true
}

internal fun UiDevice.longTap(testTag: String, timeoutMs: Long = FIND_MS): Boolean {
    val target = await(testTag, timeoutMs) ?: return false
    target.longClick()
    settle()
    return true
}

/** Off-screen Compose nodes exist in the tree with empty bounds; a click on one is a no-op. */
internal fun UiDevice.scrollTo(containerTag: String, testTag: String): UiObject2? {
    repeat(MAX_SCROLL_STEPS) {
        val target = findObject(res(testTag))
        if (target != null && target.visibleBounds.height() > 0) return target
        val container = findObject(res(containerTag)) ?: return target
        container.setGestureMargin(displayWidth / 10)
        if (!container.scroll(Direction.DOWN, 0.6f)) return findObject(res(testTag))
        settle()
    }
    return findObject(res(testTag))
}

internal fun UiObject2.typeText(value: String) {
    text = value
    SystemClock.sleep(FRAME_MS)
}

internal fun UiDevice.scrollBothWays(containerTag: String, percent: Float = 0.8f) {
    val container = await(containerTag, 5_000) ?: return
    container.setGestureMargin(displayWidth / 10)
    container.scroll(Direction.DOWN, percent)
    settle()
    container.scroll(Direction.UP, 1.0f)
    settle()
}

internal fun UiDevice.openTab(tabTag: String, contentTag: String): Boolean {
    if (!tap(tabTag, 5_000)) return false
    return awaitTag(contentTag, 10_000)
}

/**
 * Signs in if there is no persisted session. Call right after launching; it waits out the
 * splash itself. Safe in a benchmark `setupBlock` - that block is not measured.
 */
internal fun MacrobenchmarkScope.signIn() {
    device.waitForAny(20_000, "email_field", "tab_dashboard")
    if (device.hasObject(res("tab_dashboard"))) {
        log("Already signed in.")
        return
    }

    val emailField = device.await("email_field", 5_000)
        ?: error(
            "Auth screen not found: no node with resource-id 'email_field'. Check that " +
                "MainActivity's root Surface sets Modifier.semantics { testTagsAsResourceId = true }."
        )
    check(Credentials.areAvailable) { Credentials.MISSING_MESSAGE }

    log("Filling in the credentials.")
    // Set text without tapping the fields first, or the keyboard covers the sign-in button.
    emailField.typeText(Credentials.email)
    device.await("password_field", 5_000)?.typeText(Credentials.password)
        ?: error("Password field ('password_field') not found.")

    log("Signing in.")
    device.await("login_button", 5_000)?.click()
        ?: error("Sign-in button ('login_button') not found.")

    if (!device.awaitTag("tab_dashboard", 45_000)) {
        log("Dashboard not reached, retrying the sign-in tap.")
        device.await("login_button", 2_000)?.click()
        check(device.awaitTag("tab_dashboard", 45_000)) {
            "Sign-in failed: the dashboard never appeared. Check the credentials, the network " +
                "connection, and logcat for Firebase auth errors."
        }
    }
    log("Signed in.")
}

internal fun log(message: String) {
    Log.i(TAG, message)
}
