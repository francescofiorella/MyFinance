package com.frafio.myfinance.core.components

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.data.testIncome
import com.frafio.myfinance.testing.screenshot.captureMultiTheme
import com.frafio.myfinance.testing.screenshot.screenshotBarChart
import com.frafio.myfinance.testing.screenshot.screenshotExpenses
import com.frafio.myfinance.testing.screenshot.screenshotToday
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

/** Light/dark × dynamic/notDynamic captures of the shared components, as nowinandroid's designsystem tests. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-420dpi")
@LooperMode(LooperMode.Mode.PAUSED)
class ComponentScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun transactionItems_multipleThemes() {
        composeTestRule.captureMultiTheme("TransactionItems") {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    TotalItem(transaction = screenshotExpenses[0])
                    TransactionListItem(transaction = screenshotExpenses[1], indexInGroup = 0, countInGroup = 2, onClick = {}, onLongClick = {})
                    TransactionListItem(transaction = screenshotExpenses[2], indexInGroup = 1, countInGroup = 2, onClick = {}, onLongClick = {})
                    TotalItem(transaction = testIncome(price = 12000.0, category = FirestoreEnums.CATEGORIES.TOTAL.value))
                    TransactionListItem(transaction = testIncome(name = "Salary", price = 1000.0), indexInGroup = 0, countInGroup = 1, onClick = {}, onLongClick = {})
                    EmptyListItem(messageRes = R.string.no_expenses)
                }
            }
        }
    }

    @Test
    fun barChart_multipleThemes() {
        composeTestRule.captureMultiTheme("BarChart") {
            Surface(color = MaterialTheme.colorScheme.background) {
                Box(Modifier.width(360.dp).padding(16.dp)) {
                    BarChart(entries = screenshotBarChart.takeLast(6), referenceValue = 300.0)
                }
            }
        }
    }

    @Test
    fun pieChart_multipleThemes() {
        composeTestRule.captureMultiTheme("PieChart") {
            Surface(color = MaterialTheme.colorScheme.background) {
                Box(Modifier.size(360.dp)) {
                    PieChart(
                        items = listOf(
                            PieChartItem(value = 45.0, label = "Dining", icon = R.drawable.ic_home_filled),
                            PieChartItem(value = 36.5, label = "Groceries", icon = R.drawable.ic_shopping_cart_filled),
                            PieChartItem(value = 10.0, label = "Transport", icon = R.drawable.ic_grid_3x3_filled),
                        ),
                        animationSpec = snap(),
                    )
                }
            }
        }
    }

    @Test
    fun emptyView_multipleThemes() {
        composeTestRule.captureMultiTheme("EmptyView") {
            Surface(color = MaterialTheme.colorScheme.background) {
                EmptyView(modifier = Modifier.size(360.dp), image = R.drawable.image_consulting_cuate, message = R.string.warning_home)
            }
        }
    }

    @Test
    fun editTransactionSheet_multipleThemes() {
        composeTestRule.captureMultiTheme("EditTransactionSheet") {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.width(411.dp)) {
                EditTransactionSheet(
                    show = true,
                    transaction = testExpense(name = "Pizza Margherita", price = 8.5, date = screenshotToday),
                    onDismiss = {}, onLabels = {}, onEdit = {}, onDuplicate = {}, onDelete = {},
                )
            }
        }
    }

    @Test
    fun searchBar_multipleThemes() {
        composeTestRule.captureMultiTheme("SearchBar") {
            Surface(color = MaterialTheme.colorScheme.background) {
                SearchBar(query = "Pizza", onQueryChange = {}, onFilterClick = {}, modifier = Modifier.fillMaxWidth().padding(16.dp))
            }
        }
    }
}
