package com.frafio.charts

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale

/** Centre text; what the arcs expose to accessibility services is in the app's `AccessibilityTest`. */
@RunWith(RobolectricTestRunner::class)
class PieChartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val items = listOf(
        PieChartItem(value = 30.0, label = "Dining", icon = ColorPainter(Color.Black), color = Color.Red, iconColor = Color.White),
        PieChartItem(value = 45.0, label = "Groceries", icon = ColorPainter(Color.Black), color = Color.Blue, iconColor = Color.White),
    )

    /** The chart with whole euro amounts. */
    @Composable
    private fun TestPieChart(items: List<PieChartItem>, animationSpec: AnimationSpec<Float> = PieChartDefaults.AnimationSpec) =
        PieChart(
            items = items,
            totalLabel = "Total",
            valueFormatter = { "€ %.0f".format(Locale.US, it) },
            animationSpec = animationSpec
        )

    private fun setChart(content: @Composable () -> Unit) = composeTestRule.setContent { MaterialTheme(content = content) }

    @Test
    fun nothingSelected_showsTotalAndTheSum() {
        setChart {
            TestPieChart(items = items, animationSpec = snap())
        }

        composeTestRule.onNodeWithText("Total").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 75").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dining").assertDoesNotExist()
    }

    @Test
    fun emptyItems_showsTotalAndZero() {
        setChart {
            TestPieChart(items = emptyList(), animationSpec = snap())
        }

        composeTestRule.onNodeWithText("Total").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 0").assertIsDisplayed()
    }

    @Test
    fun animated_showsTheSameCentreText() {
        setChart {
            TestPieChart(items = items)
        }

        composeTestRule.onNodeWithText("Total").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 75").assertIsDisplayed()
    }
}
