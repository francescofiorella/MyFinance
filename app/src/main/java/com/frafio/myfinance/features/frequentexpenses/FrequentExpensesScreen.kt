package com.frafio.myfinance.features.frequentexpenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.AppTopBar
import com.frafio.myfinance.core.components.TotalItem
import com.frafio.myfinance.core.components.TransactionListItem
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.monthRangeToString
import java.time.LocalDate

@Composable
fun FrequentExpensesScreen(
    viewModel: FrequentExpensesViewModel,
    onBackClick: () -> Unit
) {
    val days by viewModel.days.collectAsStateWithLifecycle()
    FrequentExpensesContent(
        from = viewModel.from,
        to = viewModel.to,
        days = days,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequentExpensesContent(
    from: LocalDate,
    to: LocalDate,
    days: List<ExpensesOfDay>?,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AppTopBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(id = R.string.frequent_expenses_title), maxLines = 1)
                        Text(
                            text = monthRangeToString(from, to),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                onNavigationClick = onBackClick
            )

            LazyColumn(
                modifier = Modifier
                    .testTag("frequent_expenses_list")
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                days.orEmpty().forEach { day ->
                    item(key = "day_${day.date}") {
                        // The day's date as header, without its total.
                        TotalItem(transaction = day.expenses.first(), showPrice = false)
                    }
                    itemsIndexed(day.expenses, key = { _, expense -> expense.id }) { index, expense ->
                        // No callbacks: the rows are read-only.
                        TransactionListItem(
                            modifier = Modifier.testTag("frequent_expense_row"),
                            transaction = expense,
                            indexInGroup = index,
                            countInGroup = day.expenses.size
                        )
                    }
                }
            }
        }
    }
}

/** Fixed sample data for previews and screenshots: a name bought on three days, twice on one of them. */
fun sampleFrequentExpenseDays(to: LocalDate): List<ExpensesOfDay> {
    fun sushi(price: Double, date: LocalDate, id: String) = Expense(
        name = "Sushi Bar",
        price = price,
        year = date.year,
        month = date.monthValue,
        day = date.dayOfMonth,
        category = 5,
        labels = if (id == "s1") listOf("Dinner") else emptyList(),
        id = id
    )
    val last = to.withDayOfMonth(10)
    return groupByDay(
        listOf(
            sushi(128.0, last, "s1"),
            sushi(32.5, last, "s2"),
            sushi(114.0, last.minusMonths(1), "s3"),
            sushi(96.0, last.minusMonths(4), "s4")
        )
    )
}

@Preview(showBackground = true)
@Composable
fun FrequentExpensesPreview() {
    MyFinanceTheme {
        val to = LocalDate.of(2024, 5, 1)
        FrequentExpensesContent(
            from = to.minusMonths(11),
            to = to,
            days = sampleFrequentExpenseDays(to),
            onBackClick = {}
        )
    }
}
