package com.frafio.myfinance.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.frafio.myfinance.core.theme.MyFinanceTheme

/** Connected toggle buttons where exactly one of [options] is selected. */
@Composable
fun ConnectedToggleRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ButtonGroup(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        }
    ) {
        options.forEachIndexed { index, option ->
            customItem(
                {
                    FilledTonalToggleButton(
                        checked = index == selectedIndex,
                        onCheckedChange = {
                            if (index != selectedIndex) onSelect(index)
                        },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                        content = {
                            Text(
                                text = option,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Visible
                            )
                        }
                    )
                },
                {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectedToggleRowPreview() {
    MyFinanceTheme {
        ConnectedToggleRow(options = listOf("6M", "1Y", "2Y"), selectedIndex = 1, onSelect = {})
    }
}
