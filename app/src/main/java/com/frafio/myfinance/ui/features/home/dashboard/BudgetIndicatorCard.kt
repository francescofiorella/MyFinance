package com.frafio.myfinance.ui.features.home.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.doubleToPrice

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BudgetIndicatorCard(
    monthShown: Boolean,
    thisMonthSum: Double,
    thisYearSum: Double,
    monthlyBudget: Double,
    onToggleMonthShown: (Boolean) -> Unit
) {
    val title = if (monthlyBudget > 0.0)
        stringResource(R.string.expenses_budget)
    else
        stringResource(R.string.this_month)
    val amount = if (monthShown) thisMonthSum else thisYearSum
    val totalBudget = if (monthShown) monthlyBudget else monthlyBudget * 12

    var selectedIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(top = 32.dp, bottom = 64.dp),
        ) {
            Text(
                text = doubleToPrice(amount),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (monthlyBudget > 0.0) {
                val stroke = Stroke(
                    width =
                        with(LocalDensity.current) {
                            12.dp.toPx()
                        },
                    cap = StrokeCap.Round,
                )
                val amplitude = with(LocalDensity.current) {
                    0.2.dp.toPx()
                }
                val progress = (amount / totalBudget).toFloat()
                val animatedProgress by animateFloatAsState(
                    targetValue = progress.coerceIn(0f, 1f),
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                    label = "progressAnimation"
                )
                LinearWavyProgressIndicator(
                    color = if (progress <= 1f)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    stopSize = 8.dp,
                    amplitude = { amplitude },
                    waveSpeed = 0.dp,
                    stroke = stroke,
                    trackStroke = stroke,
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                )

                Row {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        modifier = Modifier,
                        text = doubleToPrice(totalBudget),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    ToggleButton(
                        modifier = Modifier.weight(1f),
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        checked = selectedIndex == 0,
                        onCheckedChange = {
                            selectedIndex = 0
                            onToggleMonthShown(true)
                        },
                    ) {
                        Text(
                            text = stringResource(id = R.string.monthly)
                        )
                    }
                    Spacer(modifier = Modifier.width(ButtonGroupDefaults.ConnectedSpaceBetween))
                    ToggleButton(
                        modifier = Modifier.weight(1f),
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        checked = selectedIndex == 1,
                        onCheckedChange = {
                            selectedIndex = 1
                            onToggleMonthShown(false)
                        },
                    ) {
                        Text(
                            text = stringResource(id = R.string.annual)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetIndicatorCardPreview() {
    MyFinanceTheme {
        BudgetIndicatorCard(
            monthShown = true,
            thisMonthSum = 450.0,
            thisYearSum = 5400.0,
            monthlyBudget = 1000.0,
            onToggleMonthShown = {}
        )
    }
}