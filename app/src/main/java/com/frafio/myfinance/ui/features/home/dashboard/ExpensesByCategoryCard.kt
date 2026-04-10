package com.frafio.myfinance.ui.features.home.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TonalToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.ui.components.PieChart
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.forEach

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpensesByCategoryCard(
    expenses: List<Expense>,
    date: LocalDate,
    monthlyShown: Boolean,
    onSwitchData: (Boolean) -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onToday: () -> Unit
) {
    val values = remember(expenses) {
        val vals = MutableList(9) { 0.0 }
        expenses.forEach { p ->
            if (p.category != null && p.category <= 8) {
                vals[p.category] += p.price ?: 0.0
            }
        }
        vals
    }

    val formatter = remember(monthlyShown) {
        DateTimeFormatter.ofPattern(if (monthlyShown) "MMMM uuuu" else "uuuu")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = ListItemDefaults.shapes().selectedShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.expenses_by_category),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalIconButton(
                    onClick = onPreviousDate,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallSquareShape,
                        pressedShape = IconButtonDefaults.smallRoundShape
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_keyboard_arrow_left_filled),
                        contentDescription = null
                    )
                }
                FilledTonalIconButton(
                    onClick = onNextDate,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallSquareShape,
                        pressedShape = IconButtonDefaults.smallRoundShape
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_keyboard_arrow_right_filled),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                FilledTonalButton(
                    onClick = onToday,
                    shapes = ButtonDefaults.shapes(
                        pressedShape = ButtonDefaults.squareShape
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_today_filled),
                        contentDescription = null
                    )
                }
            }

            Row(modifier = Modifier.width(IntrinsicSize.Max)) {
                TonalToggleButton(
                    modifier = Modifier.weight(1f),
                    shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                    checked = monthlyShown,
                    onCheckedChange = {
                        onSwitchData(true)
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.monthly)
                    )
                }
                Spacer(modifier = Modifier.width(ButtonGroupDefaults.ConnectedSpaceBetween))
                TonalToggleButton(
                    modifier = Modifier.weight(1f),
                    shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                    checked = !monthlyShown,
                    onCheckedChange = {
                        onSwitchData(false)
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.annual)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PieChart(
                    data = values,
                    animate = true
                )
            }

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = date.format(formatter).replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpensesByCategoryCardPreview() {
    MyFinanceTheme {
        ExpensesByCategoryCard(
            expenses = listOf(),
            date = LocalDate.now(),
            monthlyShown = true,
            onSwitchData = {},
            onPreviousDate = {},
            onNextDate = {},
            onToday = {}
        )
    }
}
