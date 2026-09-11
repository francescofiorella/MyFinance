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

/**
 * Budget for [UiDevice.waitForIdle] after an interaction. This is a ceiling, not a cost - the
 * call returns as soon as the window goes quiet, which is usually well under it.
 */
internal const val IDLE_BUDGET_MS = 600L

/**
 * Fixed pause after the window reports idle, covering the frame Compose still needs to commit.
 * Every interaction pays this in full, so keep it small - it is multiplied by roughly fifty
 * interactions per iteration and then by three to fifteen iterations.
 */
internal const val FRAME_MS = 120L

/** Default lookup timeout. Present nodes return immediately; only misses pay this. */
private const val FIND_MS = 2_000L

private const val MAX_SCROLL_STEPS = 5

/**
 * Sign-in credentials, supplied as instrumentation arguments so they never reach the repository.
 *
 * ```
 * ./gradlew :app:generateBaselineProfile \
 *     -Pandroid.testInstrumentationRunnerArguments.bpEmail=you@example.com \
 *     -Pandroid.testInstrumentationRunnerArguments.bpPassword=your-password
 * ```
 */
internal object Credentials {
    private val arguments = InstrumentationRegistry.getArguments()

    val email: String get() = arguments.getString("bpEmail").orEmpty()
    val password: String get() = arguments.getString("bpPassword").orEmpty()

    /**
     * True when both arguments were supplied. When they were not, the app may still have a
     * persisted session from an earlier run, which is enough for every journey here.
     */
    val areAvailable: Boolean get() = email.isNotEmpty() && password.isNotEmpty()

    const val MISSING_MESSAGE: String =
        "No signed-in session and no credentials. Pass them as instrumentation arguments:\n" +
            "  -Pandroid.testInstrumentationRunnerArguments.bpEmail=you@example.com\n" +
            "  -Pandroid.testInstrumentationRunnerArguments.bpPassword=your-password"
}

/**
 * Selects a node by its Compose `Modifier.testTag(...)`.
 *
 * This resolves only because the root `Surface` in `MainActivity` declares
 * `Modifier.semantics { testTagsAsResourceId = true }` - without it Compose keeps test tags to
 * itself and every lookup here returns null.
 */
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

/**
 * Lets Compose finish its transitions. [UiDevice.waitForIdle] on its own is not enough while an
 * animation is running, and it can return before the new screen has drawn - hence the short
 * fixed pause after it.
 */
internal fun UiDevice.settle() {
    waitForIdle(IDLE_BUDGET_MS)
    SystemClock.sleep(FRAME_MS)
}

/** Taps a tagged node if it is there, and reports whether it was. */
internal fun UiDevice.tap(testTag: String, timeoutMs: Long = FIND_MS): Boolean {
    val target = await(testTag, timeoutMs) ?: return false
    target.click()
    settle()
    return true
}

/** Long-presses a tagged node if it is there, and reports whether it was. */
internal fun UiDevice.longTap(testTag: String, timeoutMs: Long = FIND_MS): Boolean {
    val target = await(testTag, timeoutMs) ?: return false
    target.longClick()
    settle()
    return true
}

/**
 * Compose keeps off-screen nodes in the semantics tree with empty visible bounds, and clicking
 * one of those does nothing at all. Scroll [containerTag] until [testTag] is really on screen.
 */
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
    // The accessibility SET_TEXT action is asynchronous; give Compose a frame to apply it.
    SystemClock.sleep(FRAME_MS)
}

/** Scrolls a container both ways, which is what warms up list item composition. */
internal fun UiDevice.scrollBothWays(containerTag: String, percent: Float = 0.8f) {
    val container = await(containerTag, 5_000) ?: return
    container.setGestureMargin(displayWidth / 10)
    container.scroll(Direction.DOWN, percent)
    settle()
    container.scroll(Direction.UP, 1.0f)
    settle()
}

/**
 * Brings the app to the tab identified by [tabTag] and waits for its content.
 * Returns false when the tab bar is not on screen, so callers can skip gracefully.
 *
 * No [settle] afterwards: waiting for [contentTag] to appear already is the wait.
 */
internal fun UiDevice.openTab(tabTag: String, contentTag: String): Boolean {
    if (!tap(tabTag, 5_000)) return false
    return awaitTag(contentTag, 10_000)
}

/**
 * Signs in if needed. Safe to call when a session is already persisted - it returns immediately.
 */
internal fun MacrobenchmarkScope.signIn() {
    if (device.hasObject(res("tab_dashboard"))) {
        log("Already signed in.")
        return
    }

    val emailField = device.await("email_field", 20_000)
        ?: error(
            "Auth screen not found: no node with resource-id 'email_field'. Check that " +
                "MainActivity's root Surface sets Modifier.semantics { testTagsAsResourceId = true }."
        )
    check(Credentials.areAvailable) { Credentials.MISSING_MESSAGE }

    log("Filling in the credentials.")
    // Setting text through the accessibility action does not raise the IME, which keeps the
    // sign-in button reachable. Tapping the field first would cover it with the keyboard.
    emailField.typeText(Credentials.email)
    device.await("password_field", 5_000)?.typeText(Credentials.password)
        ?: error("Password field ('password_field') not found.")

    log("Signing in.")
    device.await("login_button", 5_000)?.click()
        ?: error("Sign-in button ('login_button') not found.")

    if (!device.awaitTag("tab_dashboard", 45_000)) {
        // The button may have sat off-screen on a short display, so the tap missed.
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
