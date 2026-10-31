package com.frafio.myfinance.features.frequentexpenses

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.myfinance.R
import com.frafio.myfinance.core.utils.monthRangeToString
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
import com.frafio.myfinance.testing.screenshot.screenshotToday
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class FrequentExpensesScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val to = screenshotToday.withDayOfMonth(1)

    @Test
    fun frequentExpenses() {
        composeTestRule.captureMultiDevice("FrequentExpensesScreen") { FrequentExpenses() }
    }

    @Test
    fun frequentExpenses_dark() {
        composeTestRule.capturePhoneDark("FrequentExpensesScreen") { FrequentExpenses() }
    }

    @Composable
    private fun FrequentExpenses() {
        FrequentExpensesContent(
            title = stringResource(id = R.string.frequent_expenses_title),
            subtitle = monthRangeToString(to.minusMonths(11), to),
            days = sampleFrequentExpenseDays(to),
            onBackClick = {}
        )
    }
}
