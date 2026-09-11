package com.frafio.myfinance.baselineProfile

import android.content.Intent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.Direction
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Generates the Baseline Profile for the app.
 *
 * ```
 * ./gradlew :app:generateBaselineProfile \
 *     -Pandroid.testInstrumentationRunnerArguments.bpEmail=you@example.com \
 *     -Pandroid.testInstrumentationRunnerArguments.bpPassword=your-password
 * ```
 *
 * ## Test order matters
 *
 * [journeysSignedIn] is the only test that signs in, and the Firebase session it leaves behind is
 * what lets the two startup tests reach the dashboard and the add screen. `NAME_ASCENDING` puts
 * `journeysSignedIn` before `startupCold` before `startupFromShortcut`, which is the order they
 * need. Renaming a test can silently break that, so keep the initials in order.
 *
 * ## Startup profiles are kept small on purpose
 *
 * Only the two `startup*` tests set `includeInStartupProfile = true`. Startup profiles also drive
 * dex layout, so padding them with a long journey makes startup worse, not better. Everything
 * else goes into the main profile.
 *
 * ## This never writes to Firestore
 *
 * Every journey is read-only. Forms are filled in but never saved, sheets are opened but
 * dismissed without choosing a destructive action, and the delete / duplicate / label entries of
 * the transaction sheet are deliberately never tapped.
 *
 * ## Logs
 *
 * `println` from an instrumentation test does not reach logcat. Follow progress with:
 * ```
 * adb logcat -s MyFinanceBP
 * ```
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    /**
     * Walks the whole app: auth, the four Home tabs, the dashboard charts, search and filtering,
     * the transaction sheet, the profile sub-screens and the add screen.
     *
     * Excluded from the startup profile - none of this is needed to draw the first frame.
     */
    @Test
    fun journeysSignedIn() {
        rule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = false,
            // Default is 15. This journey is long, and in practice it settles within a handful of
            // runs, so cap the worst case. Falling short of `stableIterations` only logs a
            // warning and keeps what it collected - it does not fail the run.
            maxIterations = 6
        ) {
            pressHome()
            startActivityAndWait()
            // Past the splash screen: either the auth screen or the dashboard is now up.
            device.waitForAny(20_000, "email_field", "tab_dashboard")

            signUpFormToggle()
            signIn()

            dashboardJourney()
            expensesJourney()
            budgetJourney()
            profileJourney()
            addTransactionJourney()
        }
    }

    /**
     * Cold start straight to the dashboard - the path a returning user takes every day.
     * Kept to nothing but the launch so the startup profile stays focused.
     */
    @Test
    fun startupCold() {
        rule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()
            device.waitForAny(20_000, "tab_dashboard", "email_field")
        }
    }

    /**
     * Cold start through the launcher shortcuts, which land directly on the add screen.
     * This is a startup path in its own right - it is what `StartupBenchmarks` measures - so it
     * needs the dex-layout treatment that only a startup profile applies.
     */
    @Test
    fun startupFromShortcut() {
        rule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = true
        ) {
            pressHome()
            launchShortcut("com.frafio.myfinance.ADD_EXPENSE")

            // Kill in between so the second launch is genuinely cold as well.
            killProcess()
            pressHome()
            launchShortcut("com.frafio.myfinance.ADD_INCOME")
        }
    }

    // ---------------------------------------------------------------------------------------
    // Journeys
    // ---------------------------------------------------------------------------------------

    /** Expands and collapses the sign-up form so its composables land in the profile too. */
    private fun MacrobenchmarkScope.signUpFormToggle() {
        if (device.hasObject(res("tab_dashboard"))) return
        if (!device.tap("auth_toggle_mode")) {
            log("Auth screen not shown, skipping the sign-up form.")
            return
        }
        log("Toggling the sign-up form.")
        device.tap("auth_toggle_mode")
    }

    /** Dashboard: scroll the cards, then drive the bar and pie charts. */
    private fun MacrobenchmarkScope.dashboardJourney() {
        log("Dashboard.")
        if (!device.openTab("tab_dashboard", "dashboard_scroll")) return
        device.scrollBothWays("dashboard_scroll")

        // The charts draw on a Canvas, so their draw code benefits from AOT more than ordinary
        // layout does. One step each way plus today covers the redraw path - repeating a step
        // re-runs code the profile already has.
        log("Bar chart.")
        device.scrollTo("dashboard_scroll", "bar_chart_prev")
        device.tap("bar_chart_prev")
        device.tap("bar_chart_next")
        device.tap("bar_chart_today")

        log("Pie chart.")
        device.scrollTo("dashboard_scroll", "pie_chart_prev")
        device.tap("pie_chart_prev")
        device.tap("pie_chart_annual")
        device.tap("pie_chart_monthly")
    }

    /** Expenses: fling the list, search, open the filter sheets, open a transaction for editing. */
    private fun MacrobenchmarkScope.expensesJourney() {
        log("Expenses.")
        if (!device.openTab("tab_expenses", "expenses_list")) return

        device.await("expenses_list", 5_000)?.let {
            it.setGestureMargin(device.displayWidth / 10)
            it.fling(Direction.DOWN)
            device.settle()
            it.fling(Direction.UP)
            device.settle()
        }

        // Searching re-runs the whole list composition through a different path than scrolling.
        device.await("search_field", 5_000)?.let {
            log("Searching.")
            it.typeText("a")
            device.settle()
            it.typeText("")
            device.settle()
        }

        // Filter sheet -> category grid. Dismissed without choosing, so no filter is left behind.
        if (device.tap("search_filter_button")) {
            log("Filter sheet.")
            if (device.tap("filter_category")) {
                device.settle()
                device.pressBack()
                device.settle()
            }
        }

        // Filter sheet -> date range picker. A heavy Material 3 component worth composing once.
        if (device.tap("search_filter_button")) {
            log("Date range picker.")
            if (device.tap("filter_date_range")) {
                device.settle()
                device.pressBack()
                device.settle()
            }
        }

        transactionEditJourney()
    }

    /**
     * Long-presses a transaction to open its action sheet, then opens the add screen in edit
     * mode - a different code path from adding - and leaves without saving.
     *
     * The sheet's delete, duplicate and label entries are never tapped: those write to Firestore.
     */
    private fun MacrobenchmarkScope.transactionEditJourney() {
        // Long-press the row body, never the leading category icon: that icon opens a category
        // picker bound to the transaction, where a tap would update it in Firestore.
        if (!device.longTap("expense_item", 10_000)) {
            log("No transaction to open, skipping the edit journey.")
            return
        }
        log("Transaction sheet.")

        if (!device.tap("transaction_edit")) {
            log("Edit entry not found, dismissing the sheet.")
            device.pressBack()
            device.settle()
            return
        }

        if (device.awaitTag("add_name_field", 10_000)) {
            log("Add screen in edit mode.")
            device.settle()
        }
        closeAddScreen()
        device.awaitTag("expenses_list", 10_000)
        device.settle()
    }

    private fun MacrobenchmarkScope.budgetJourney() {
        log("Budget.")
        if (!device.openTab("tab_budget", "budget_list")) return
        device.scrollBothWays("budget_list")
    }

    private fun MacrobenchmarkScope.profileJourney() {
        log("Profile.")
        if (!device.openTab("tab_profile", "profile_scroll")) return

        // Expand the account section, then collapse it again.
        if (device.tap("profile_edit_profile")) {
            device.tap("profile_edit_profile")
        }

        openAndClose("profile_manage_categories", "categories_scroll")
        openAndClose("profile_manage_labels", "labels_list")
        openAndClose("profile_change_password", "change_password_scroll")

        // Currency picker - a bottom sheet rather than a separate destination.
        device.scrollTo("profile_scroll", "profile_change_currency")?.let {
            log("Currency sheet.")
            it.click()
            device.settle()
            device.pressBack()
            device.settle()
        }

        device.await("profile_scroll", 5_000)?.let {
            it.setGestureMargin(device.displayWidth / 10)
            it.scroll(Direction.UP, 1.0f)
        }
        device.settle()
    }

    /** Opens a profile sub-screen, waits for it to draw, then navigates back. */
    private fun MacrobenchmarkScope.openAndClose(rowTag: String, screenTag: String) {
        val row = device.scrollTo("profile_scroll", rowTag) ?: run {
            log("Profile row '$rowTag' not found, skipping.")
            return
        }
        log("Opening '$rowTag'.")
        row.click()
        if (!device.awaitTag(screenTag, 10_000)) {
            log("'$screenTag' never appeared after opening '$rowTag'.")
        }
        device.settle()
        device.pressBack()
        device.awaitTag("profile_scroll", 10_000)
        device.settle()
    }

    /**
     * Opens the add-transaction screen from the FAB, fills it in and switches the transaction
     * type. It never taps save - the generator runs against a real account.
     */
    private fun MacrobenchmarkScope.addTransactionJourney() {
        log("Add transaction.")
        device.openTab("tab_dashboard", "dashboard_scroll")

        if (!device.tap("add_fab", 10_000)) {
            log("FAB ('add_fab') not found, skipping.")
            return
        }

        val nameField = device.await("add_name_field", 10_000) ?: run {
            log("Add screen never appeared.")
            return
        }
        nameField.typeText("Baseline profile")
        device.settle()
        device.await("add_amount_field", 5_000)?.typeText("12.34")
        device.settle()

        // Expense / income switcher.
        if (device.tap("add_type_button")) {
            device.pressBack()
            device.settle()
        }

        closeAddScreen()
        device.awaitTag("tab_dashboard", 10_000)
        device.settle()
    }

    /** Launches the add screen through a launcher shortcut intent. */
    private fun MacrobenchmarkScope.launchShortcut(action: String) {
        startActivityAndWait(
            Intent().apply {
                this.action = action
                `package` = PACKAGE_NAME
            }
        )
        if (!device.awaitTag("add_name_field", 15_000)) {
            log(
                "Add screen never appeared for $action. The shortcut only opens it for a " +
                    "signed-in user - run journeysSignedIn first."
            )
        }
    }

    private fun MacrobenchmarkScope.closeAddScreen() {
        if (!device.tap("add_close_button")) {
            device.pressBack()
            device.settle()
        }
    }
}
