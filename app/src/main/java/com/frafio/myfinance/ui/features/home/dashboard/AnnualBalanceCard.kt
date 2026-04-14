package com.frafio.myfinance.ui.features.home.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.doubleToPriceWithoutDecimals
import kotlin.math.abs

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnnualBalanceCard(
    balanceYear: Int,
    incomesSum: Double,
    expensesSum: Double,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onToday: () -> Unit
) {
    val balance = incomesSum - expensesSum

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.annual_balance),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = balanceYear.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                FilledTonalIconButton(
                    onClick = onPreviousYear,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallSquareShape,
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_keyboard_arrow_left_filled),
                        contentDescription = null
                    )
                }
                FilledTonalIconButton(
                    onClick = onNextYear,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallSquareShape,
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

            Text(
                text = doubleToPrice(abs(balance)),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (balance < 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(MaterialShapes.Pill.toShape())
                            .background(
                                if (isSystemInDarkTheme())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_downward_filled),
                            contentDescription = null,
                            tint = if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = stringResource(R.string.incomes),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (incomesSum < 1000)
                                doubleToPrice(incomesSum)
                            else
                                doubleToPriceWithoutDecimals(incomesSum),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(MaterialShapes.Pill.toShape())
                            .background(
                                if (isSystemInDarkTheme())
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.errorContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_upward_filled),
                            contentDescription = null,
                            tint = if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.onError
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = stringResource(R.string.expenses),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (expensesSum < 1000)
                                doubleToPrice(expensesSum)
                            else
                                doubleToPriceWithoutDecimals(expensesSum),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnnualBalanceCardPreview() {
    MyFinanceTheme {
        AnnualBalanceCard(
            balanceYear = 2024,
            incomesSum = 2000.0,
            expensesSum = 1500.0,
            onPreviousYear = {},
            onNextYear = {},
            onToday = {}
        )
    }
}