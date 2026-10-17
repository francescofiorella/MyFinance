
package com.frafio.myfinance.core.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.theme.MyFinanceTheme

/** Previous / next / back-to-today buttons for stepping through months or years. */
@Composable
fun PeriodStepper(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    previousDescription: String,
    nextDescription: String,
    todayDescription: String,
    modifier: Modifier = Modifier,
    isPreviousEnabled: Boolean = true,
    isNextEnabled: Boolean = true
) {
    val previousInteractionSource = remember { MutableInteractionSource() }
    val nextInteractionSource = remember { MutableInteractionSource() }
    val todayInteractionSource = remember { MutableInteractionSource() }
    ButtonGroup(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        }
    ) {
        customItem(
            {
                FilledTonalIconButton(
                    modifier = Modifier
                        .size(IconButtonDefaults.smallContainerSize())
                        .animateWidth(previousInteractionSource),
                    onClick = onPrevious,
                    enabled = isPreviousEnabled,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallSquareShape,
                    ),
                    interactionSource = previousInteractionSource
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_keyboard_arrow_left_filled),
                        contentDescription = previousDescription,
                    )
                }
            },
            {}
        )
        customItem(
            {
                FilledTonalIconButton(
                    modifier = Modifier
                        .size(IconButtonDefaults.smallContainerSize())
                        .animateWidth(nextInteractionSource),
                    onClick = onNext,
                    enabled = isNextEnabled,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallSquareShape,
                    ),
                    interactionSource = nextInteractionSource
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_keyboard_arrow_right_filled),
                        contentDescription = nextDescription
                    )
                }
            },
            {}
        )
        customItem(
            {
                FilledTonalIconButton(
                    modifier = Modifier
                        .width(52.dp)
                        .animateWidth(todayInteractionSource),
                    onClick = onToday,
                    shapes = IconButtonDefaults.shapes(),
                    interactionSource = todayInteractionSource
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_today_filled),
                        contentDescription = todayDescription,
                    )
                }
            },
            {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PeriodStepperPreview() {
    MyFinanceTheme {
        PeriodStepper(
            onPrevious = {},
            onNext = {},
            onToday = {},
            previousDescription = stringResource(R.string.previous_month),
            nextDescription = stringResource(R.string.next_month),
            todayDescription = stringResource(R.string.current_month),
            isNextEnabled = false
        )
    }
}
