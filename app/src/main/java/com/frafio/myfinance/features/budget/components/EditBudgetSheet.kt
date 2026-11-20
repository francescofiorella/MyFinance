package com.frafio.myfinance.features.budget.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.AppBottomSheet
import com.frafio.myfinance.core.components.SheetDialog
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.doubleToString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetSheet(
    show: Boolean,
    budget: Double,
    onDismiss: () -> Unit,
    onEditBudget: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Reset whenever the sheet opens; saveable so typed text survives rotation.
    val budgetState = rememberSaveable(show, saver = TextFieldState.Saver) {
        TextFieldState(if (budget == 0.0) "" else doubleToString(budget))
    }

    val isConfirmEnabled by remember(budgetState, budget) {
        derivedStateOf {
            val newBudget = budgetState.text.toString().toDoubleOrNull()
            newBudget != null && newBudget != budget
        }
    }

    val confirm = {
        if (isConfirmEnabled) {
            onEditBudget(budgetState.text.toString().toDoubleOrNull() ?: 0.0)
            onDismiss()
        }
    }

    AppBottomSheet(
        show = show,
        onDismiss = onDismiss
    ) {
        SheetDialog(
            icon = R.drawable.ic_savings_filled,
            title = stringResource(id = R.string.your_budget),
            label = stringResource(id = R.string.edit),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    state = budgetState,
                    modifier = Modifier
                        .weight(1f),
                    label = { Text(stringResource(id = R.string.enter_your_budget)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit_outline),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Decimal
                    ),
                    inputTransformation = MaxTwoDecimals,
                    onKeyboardAction = { confirm() },
                    lineLimits = TextFieldLineLimits.SingleLine
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = confirm,
                    enabled = isConfirmEnabled,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallSquareShape,
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check_filled),
                        contentDescription = stringResource(id = R.string.confirm)
                    )
                }
            }
        }
    }
}

// Truncates the decimals typed past the second one.
private val MaxTwoDecimals = InputTransformation {
    val parts = asCharSequence().toString().split(".")
    if (parts.size > 1 && parts[1].length > 2) {
        replace(0, length, "${parts[0]}.${parts[1].substring(0, 2)}")
    }
}

@Preview(showBackground = true)
@Composable
fun EditBudgetSheetPreview() {
    MyFinanceTheme {
        EditBudgetSheet(
            show = true,
            budget = 0.0,
            onDismiss = {},
            onEditBudget = {},
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
