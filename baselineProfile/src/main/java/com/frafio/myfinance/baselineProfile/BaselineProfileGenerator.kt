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
 * Generates the Baseline Profile. See README.md for the command, what to expect on screen, and
 * the benchmark numbers behind the choices here.
 *
 * Test order is load-bearing: [journeysSignedIn] is the only test that signs in, and the two
 * `startup*` tests depend on the session it leaves behind. `NAME_ASCENDING` gives that order;
 * keep the names sorting that way.
 *
 * Every journey is read-only. Nothing is ever saved, and the transaction sheet's delete,
 * duplicate and label entries are never tapped - those write to Firestore.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun journeysSignedIn() {
        rule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = false,
            maxIterations = 6
        ) {
            pressHome()
            startActivityAndWait()
            device.waitForAny(20_000, "email_field", "tab_dashboard")

            signUpFormToggle()
            signIn()

            expensesJourney()
            budgetJourney()
            profileJourney()
            addTransactionJourney()
        }
    }

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

    @Test
    fun startupFromShortcut() {
        rule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = true
        ) {
            pressHome()
            launchShortcut("com.frafio.myfinance.ADD_EXPENSE")

            killProcess()
            pressHome()
            launchShortcut("com.frafio.myfinance.ADD_INCOME")
        }
    }

    private fun MacrobenchmarkScope.signUpFormToggle() {
        if (device.hasObject(res("tab_dashboard"))) return
        if (!device.tap("auth_toggle_mode")) {
            log("Auth screen not shown, skipping the sign-up form.")
            return
        }
        log("Toggling the sign-up form.")
        device.tap("auth_toggle_mode")
    }

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

        device.await("search_field", 5_000)?.let {
            log("Searching.")
            it.typeText("a")
            device.settle()
            it.typeText("")
            device.settle()
        }

        if (device.tap("search_filter_button")) {
            log("Filter sheet.")
            if (device.tap("filter_category")) {
                device.settle()
                device.pressBack()
                device.settle()
            }
        }

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

    private fun MacrobenchmarkScope.transactionEditJourney() {
        // Long-press the row body, not the category icon: the icon's picker writes to Firestore.
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

        if (device.tap("profile_edit_profile")) {
            device.tap("profile_edit_profile")
        }

        openAndClose("profile_manage_categories", "categories_scroll")
        openAndClose("profile_manage_labels", "labels_list")
        openAndClose("profile_change_password", "change_password_scroll")

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

        if (device.tap("add_type_button")) {
            device.pressBack()
            device.settle()
        }

        closeAddScreen()
        device.awaitTag("tab_dashboard", 10_000)
        device.settle()
    }

    private fun MacrobenchmarkScope.launchShortcut(action: String) {
        startActivityAndWait(
            Intent().apply {
                this.action = action
                `package` = PACKAGE_NAME
            }
        )
        if (!device.awaitTag("add_name_field", 15_000)) {
            log("Add screen never appeared for $action - the shortcut needs a signed-in session.")
        }
    }

    private fun MacrobenchmarkScope.closeAddScreen() {
        if (!device.tap("add_close_button")) {
            device.pressBack()
            device.settle()
        }
    }
}
