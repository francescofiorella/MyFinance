package com.frafio.myfinance.features.budget

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.captureForDevice
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
import com.frafio.myfinance.testing.screenshot.screenshotIncomes
import com.frafio.myfinance.testing.screenshot.screenshotIncomesMetadata
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class BudgetScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun populated() {
        composeTestRule.captureMultiDevice("BudgetScreenPopulated") { Budget(screenshotIncomes, isEmpty = false, monthlyBudget = 1200.0) }
    }

    @Test
    fun populated_dark() {
        composeTestRule.capturePhoneDark("BudgetScreenPopulated") { Budget(screenshotIncomes, isEmpty = false, monthlyBudget = 1200.0) }
    }

    @Test
    fun empty() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "BudgetScreenEmpty", deviceName = "phone") {
            Budget(emptyList(), isEmpty = true, monthlyBudget = 0.0)
        }
    }

    @Composable
    private fun Budget(incomes: List<Income>, isEmpty: Boolean?, monthlyBudget: Double) {
        BudgetContent(
            incomes = incomes,
            isIncomesEmpty = isEmpty,
            monthlyBudget = monthlyBudget,
            annualBudget = monthlyBudget * 12,
            onEditBudgetClick = {},
            onDeleteBudget = {},
            itemMetadata = screenshotIncomesMetadata,
            onItemLongClick = { _, _ -> },
            onLoadMore = {},
            scrollToIdFlow = flowOf(null),
        )
    }
}
