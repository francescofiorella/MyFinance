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

        // Before searching: the expanded search bar hides these buttons.
        categoriesJourney()
        openAndClose("expenses_list", "expenses_manage_labels", "labels_list")

        device.await("search_field", 5_000)?.let {
            log("Searching.")
            // Focusing the field expands the bar over the manage buttons.
            it.click()
            device.settle()
            it.typeText("a")
            device.settle()
            it.typeText("")
            device.settle()
        }
        if (device.tap("search_back_button")) {
            log("Search collapsed.")
        }

        if (device.tap("search_filter_button")) {
            log("Filter sheet.")
            if (device.tap("filter_category")) {
                device.settle()
                device.pressBack()
                device.settle()
            } else {
                closeSheet("filter_category")
            }
        }

        if (device.tap("search_filter_button")) {
            log("Date range picker.")
            if (device.tap("filter_date_range")) {
                device.settle()
                device.pressBack()
                device.settle()
            } else {
                closeSheet("filter_date_range")
            }
        }

        transactionEditJourney()
    }

    /**
     * The Categories overview, then the detail page of its first category. "See all expenses" is left
     * untouched: it would replace the Expenses filters the rest of the journey relies on.
     */
    private fun MacrobenchmarkScope.categoriesJourney() {
        val entry = device.scrollTo("expenses_list", "expenses_categories") ?: run {
            log("Entry 'expenses_categories' not found, skipping.")
            return
        }
        log("Categories.")
        entry.click()
        if (!device.awaitTag("categories_scroll", 10_000)) {
            log("'categories_scroll' never appeared.")
        }
        device.awaitGone("expenses_list", 5_000)
        device.settle()

        if (device.tap("category_item")) {
            log("Category detail.")
            if (device.awaitTag("category_detail_scroll", 10_000)) {
                device.awaitGone("categories_scroll", 5_000)
                device.settle()
                device.scrollBothWays("category_detail_scroll")
                frequentExpensesJourney()
            } else {
                log("'category_detail_scroll' never appeared.")
            }
            device.pressBack()
            confirmBack("categories_scroll", "category_detail_scroll")
        } else {
            log("No category row ('category_item'), skipping the detail.")
        }

        device.pressBack()
        confirmBack("expenses_list", "categories_scroll")
    }

    // A sheet left open would cover every later step: close it when its entry is missing.
    private fun MacrobenchmarkScope.closeSheet(missingTag: String) {
        log("'$missingTag' not found, closing the sheet.")
        device.pressBack()
        device.settle()
    }

    // Needs the same expense name at least twice in the category, within the 12 months shown.
    private fun MacrobenchmarkScope.frequentExpensesJourney() {
        val entry = device.scrollTo("category_detail_scroll", "frequent_expense_item") ?: run {
            log("No frequent expense ('frequent_expense_item'), skipping.")
            return
        }
        log("Frequent expenses.")
        entry.click()
        if (device.awaitTag("frequent_expenses_list", 10_000)) {
            device.awaitGone("category_detail_scroll", 5_000)
            device.settle()
            device.scrollBothWays("frequent_expenses_list")
        } else {
            log("'frequent_expenses_list' never appeared.")
        }
        device.pressBack()
        confirmBack("category_detail_scroll", "frequent_expenses_list")
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
        confirmBack("expenses_list", "add_name_field") { closeAddScreen() }
    }

    private fun MacrobenchmarkScope.budgetJourney() {
        log("Budget.")
        if (!device.openTab("tab_budget", "budget_list")) return
        device.scrollBothWays("budget_list")
    }

    private fun MacrobenchmarkScope.profileJourney() {
        log("Profile.")
        if (!device.openTab("tab_profile", "profile_scroll")) return

        // Change password is the last row of the edit section: open it while the section is expanded, then collapse it.
        if (device.tap("profile_edit_profile")) {
            openAndClose("profile_scroll", "profile_change_password", "change_password_scroll")
            device.tap("profile_edit_profile")
        }

        // Labels and the spending overview are opened from the Expenses tab; category management from here.
        manageCategoriesJourney()

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

    private fun MacrobenchmarkScope.manageCategoriesJourney() {
        val entry = device.scrollTo("profile_scroll", "profile_manage_categories") ?: run {
            log("Entry 'profile_manage_categories' not found, skipping.")
            return
        }
        log("Manage categories.")
        entry.click()
        if (device.awaitTag("manage_categories_list", 10_000)) {
            device.awaitGone("profile_scroll", 5_000)
            device.settle()
            if (device.tap("manage_category_item")) {
                log("Category description expanded.")
                device.settle()
            }
            device.scrollBothWays("manage_categories_list")
        } else {
            log("'manage_categories_list' never appeared.")
        }
        device.pressBack()
        confirmBack("profile_scroll", "manage_categories_list")
    }

    private fun MacrobenchmarkScope.openAndClose(containerTag: String, entryTag: String, screenTag: String) {
        val entry = device.scrollTo(containerTag, entryTag) ?: run {
            log("Entry '$entryTag' not found, skipping.")
            return
        }
        log("Opening '$entryTag'.")
        entry.click()
        if (!device.awaitTag(screenTag, 10_000)) {
            log("'$screenTag' never appeared after opening '$entryTag'.")
        }
        device.awaitGone(containerTag, 5_000)
        device.settle()
        device.pressBack()
        confirmBack(containerTag, screenTag)
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
        confirmBack("tab_dashboard", "add_name_field") { closeAddScreen() }
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

    /**
     * Waits for [expectedTag] after closing [openTag]. If it does not come back, logs whether [openTag] is
     * still on screen, saves a screenshot, and runs [closeAgain] only when [openTag] really is still there.
     */
    private fun MacrobenchmarkScope.confirmBack(
        expectedTag: String,
        openTag: String,
        closeAgain: () -> Unit = { device.pressBack() }
    ) {
        if (!device.awaitTag(expectedTag, 10_000)) {
            val stillOpen = device.hasObject(res(openTag))
            log("'$expectedTag' not back after closing '$openTag' ('$openTag' still visible: $stillOpen).")
            device.saveScreenshot("not-back-after-$openTag")
            if (stillOpen) {
                log("Closing '$openTag' again.")
                closeAgain()
                device.awaitTag(expectedTag, 10_000)
            }
        }
        device.settle()
    }

    private fun MacrobenchmarkScope.closeAddScreen() {
        if (!device.tap("add_close_button")) {
            device.pressBack()
            device.settle()
        }
    }
}
