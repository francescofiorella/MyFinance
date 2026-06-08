package com.frafio.myfinance.features.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.core.utils.doubleToPriceWithoutDecimals
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpensesCard(
    todaySum: Double,
    monthShown: Boolean,
    thisMonthSum: Double,
    thisYearSum: Double
) {
    val now = LocalDate.now()
    val day = now.format(DateTimeFormatter.ofPattern("dd"))
    val month = now.format(DateTimeFormatter.ofPattern("MMMM")).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
    val year = now.year

    val colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )

    Column {
        SegmentedListItem(
            onClick = { },
            shapes = ListItemDefaults.segmentedShapes(
                index = 0,
                count = 2,
                defaultShapes = ListItemDefaults.shapes()
            ),
            colors = colors,
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialShapes.Pill.toShape())
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_today_filled),
                        contentDescription = null,
                    )
                }
            },
            content = {
                Text(
                    text = stringResource(R.string.expenses_today),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            supportingContent = {
                Text(
                    text = "$day $month $year",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Text(
                    modifier = Modifier
                        .padding(end = 8.dp),
                    text = if (todaySum < 1000)
                        doubleToPrice(todaySum)
                    else
                        doubleToPriceWithoutDecimals(todaySum),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 2.dp)
        )
        val title = if (monthShown)
            stringResource(R.string.this_year_next)
        else
            stringResource(R.string.this_month)

        val subTitle = if (monthShown) year.toString() else "$month $year"

        val amount = if (monthShown) thisYearSum else thisMonthSum

        SegmentedListItem(
            onClick = { },
            shapes = ListItemDefaults.segmentedShapes(
                index = 1,
                count = 2,
                defaultShapes = ListItemDefaults.shapes()
            ),
            colors = colors,
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialShapes.Sunny.toShape())
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar_month_filled),
                        contentDescription = null,
                    )
                }
            },
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            supportingContent = {
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = if (todaySum < 1000)
                        doubleToPrice(amount)
                    else
                        doubleToPriceWithoutDecimals(amount),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExpensesCardPreview() {
    MyFinanceTheme {
        ExpensesCard(
            todaySum = 25.0,
            monthShown = true,
            thisMonthSum = 450.0,
            thisYearSum = 5400.0
        )
    }
}