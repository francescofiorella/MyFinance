package com.frafio.charts

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/** Centre text and arc selection; what the arcs expose to accessibility services is in the app's `AccessibilityTest`. */
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

    private fun arc(label: String, value: String) =
        composeTestRule.onNodeWithContentDescription("$label, $value")

    /** The middle of [items]' arc at [index], in the chart's coordinates. */
    private fun TouchInjectionScope.arcMiddle(index: Int): Offset {
        val gap = arcGapDegrees(
            PieChartDefaults.OffsetBetweenArcs.value,
            PieChartDefaults.ArcWidth.value,
            PieChartDefaults.Radius.value
        )
        val sweeps = arcSweeps(items.map { it.value }, gap)
        val start = -90f + (0 until index).sumOf { (sweeps[it] + gap).toDouble() }.toFloat()
        val angle = Math.toRadians((start + sweeps[index] / 2).toDouble())
        val radius = PieChartDefaults.Radius.toPx()
        return center + Offset(radius * cos(angle).toFloat(), radius * sin(angle).toFloat())
    }

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

    @Test
    fun tapOnArc_selectsIt() {
        setChart {
            TestPieChart(items = items, animationSpec = snap())
        }

        arc("Dining", "€ 30").performTouchInput { click(arcMiddle(0)) }

        arc("Dining", "€ 30").assertIsSelected()
        composeTestRule.onNodeWithText("Dining").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 30").assertIsDisplayed()
    }

    @Test
    fun heldPress_selectsOnlyOnRelease() {
        setChart {
            TestPieChart(items = items, animationSpec = snap())
        }

        arc("Groceries", "€ 45").performTouchInput { down(arcMiddle(1)) }

        arc("Groceries", "€ 45").assertIsNotSelected()
        composeTestRule.onNodeWithText("Total").assertIsDisplayed()

        arc("Groceries", "€ 45").performTouchInput { up() }

        arc("Groceries", "€ 45").assertIsSelected()
        composeTestRule.onNodeWithText("Groceries").assertIsDisplayed()
    }

    @Test
    fun pressDraggedOffTheChart_doesNotSelect() {
        setChart {
            TestPieChart(items = items, animationSpec = snap())
        }

        arc("Dining", "€ 30").performTouchInput {
            down(arcMiddle(0))
            moveTo(Offset(width * 2f, height * 2f))
            up()
        }

        arc("Dining", "€ 30").assertIsNotSelected()
        composeTestRule.onNodeWithText("Total").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun focusedArc_selectsOnEnter() {
        lateinit var inputModeManager: InputModeManager
        setChart {
            inputModeManager = LocalInputModeManager.current
            TestPieChart(items = items, animationSpec = snap())
        }
        composeTestRule.runOnIdle { inputModeManager.requestInputMode(InputMode.Keyboard) }

        arc("Dining", "€ 30")
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .assertIsFocused()
            .performKeyInput { pressKey(Key.Enter) }

        arc("Dining", "€ 30").assertIsSelected()
        composeTestRule.onNodeWithText("Dining").assertIsDisplayed()
    }
}
