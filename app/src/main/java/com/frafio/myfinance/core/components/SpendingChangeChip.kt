package com.frafio.myfinance.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.SpendingChange
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.doubleToPercent
import kotlin.math.abs

/** ↑ / ↓ and the percentage versus last month, or "New"; nothing when neither month has spending. */
@Composable
fun SpendingChangeChip(
    modifier: Modifier = Modifier,
    change: SpendingChange,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge
) {
    when (change) {
        SpendingChange.None -> {
            val description = stringResource(id = R.string.spending_none_description)
            Text(
                modifier = modifier
                    .padding(start = 4.dp)
                    .clearAndSetSemantics { contentDescription = description },
                text = stringResource(id = R.string.none_category_spending),
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SpendingChange.New -> {
            val description = stringResource(id = R.string.spending_new_description)
            Text(
                modifier = modifier
                    .padding(start = 4.dp)
                    .clearAndSetSemantics { contentDescription = description },
                text = stringResource(id = R.string.new_category_spending),
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        is SpendingChange.Percent -> {
            val percent = doubleToPercent(abs(change.value))
            val isUp = change.value > 0.0
            val color = when {
                change.value > 0.0 -> MaterialTheme.colorScheme.error
                change.value < 0.0 -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            val description = stringResource(
                id = if (isUp) R.string.spending_up_percent else R.string.spending_down_percent,
                percent
            )
            Row(
                modifier = modifier.clearAndSetSemantics { contentDescription = description },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (change.value != 0.0) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(
                            id = if (isUp) R.drawable.ic_arrow_upward_filled else R.drawable.ic_arrow_downward_filled
                        ),
                        contentDescription = null,
                        tint = color
                    )
                }
                Text(
                    text = percent,
                    style = textStyle,
                    color = color
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SpendingChangeChipPreview() {
    MyFinanceTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column {
                SpendingChangeChip(change = SpendingChange.Percent(0.12))
                SpendingChangeChip(change = SpendingChange.Percent(-0.08))
                SpendingChangeChip(change = SpendingChange.New)
                SpendingChangeChip(change = SpendingChange.None)
            }
        }
    }
}
