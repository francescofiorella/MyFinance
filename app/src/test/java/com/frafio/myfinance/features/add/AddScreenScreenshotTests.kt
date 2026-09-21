package com.frafio.myfinance.features.add

import androidx.activity.ComponentActivity
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.captureForDevice
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class AddScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun addExpense() {
        composeTestRule.captureMultiDevice("AddScreenExpense") { Add(AddViewModel.REQUEST_EXPENSE_CODE) }
    }

    @Test
    fun addExpense_dark() {
        composeTestRule.capturePhoneDark("AddScreenExpense") { Add(AddViewModel.REQUEST_EXPENSE_CODE) }
    }

    @Test
    fun addIncome() {
        composeTestRule.captureMultiDevice("AddScreenIncome") {
            Add(AddViewModel.REQUEST_INCOME_CODE, name = "Salary", price = "2500", category = FirestoreEnums.CATEGORIES.INCOME.value, labels = emptyList())
        }
    }

    @Test
    fun addExpense_withErrors() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "AddScreenExpenseErrors", deviceName = "phone") {
            Add(AddViewModel.REQUEST_EXPENSE_CODE, name = "", price = "", category = -1, nameError = "Enter a name", priceError = "Enter an amount", categoryError = "Pick a category")
        }
    }

    @Composable
    private fun Add(
        expenseCode: Int,
        name: String = "Pizza Margherita",
        price: String = "8.50",
        category: Int = FirestoreEnums.CATEGORIES.DINING.value,
        labels: List<String> = listOf("Dinner", "Cheat Meal"),
        nameError: String? = null,
        priceError: String? = null,
        categoryError: String? = null,
    ) {
        AddScreen(
            appState = rememberMyFinanceAppState(),
            isAdding = true,
            nameState = rememberTextFieldState(name),
            priceState = rememberTextFieldState(price),
            dateString = "29 May 2024",
            onDateClick = {},
            category = category,
            onCategoryClick = {},
            labels = labels,
            onLabelClick = {},
            onLabelCheckedChanged = { _, _ -> },
            navKey = RootKey.AddEditTransaction(requestType = RootKey.RequestType.Add, expenseCode = expenseCode),
            onNavKeyChange = {},
            onSaveClick = {},
            onBackClick = {},
            nameError = nameError,
            priceError = priceError,
            categoryError = categoryError,
        )
    }
}
