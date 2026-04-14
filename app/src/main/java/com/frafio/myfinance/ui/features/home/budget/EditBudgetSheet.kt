package com.frafio.myfinance.ui.features.home.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.components.SheetDialog
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.doubleToString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditBudgetSheet(
    budget: Double,
    onDismiss: () -> Unit,
    onEditBudget: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialText = if (budget == 0.0) "" else doubleToString(budget)
    var budgetTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length)
            )
        )
    }

    val isConfirmEnabled = remember(budgetTextFieldValue.text) {
        val newBudget = budgetTextFieldValue.text.toDoubleOrNull() ?: 0.0
        budgetTextFieldValue.text.isNotEmpty() && newBudget != budget
    }

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
                value = budgetTextFieldValue,
                onValueChange = {
                    var filteredValue = it.text
                    if (it.text.contains(".")) {
                        val parts = it.text.split(".")
                        if (parts.size > 1 && parts[1].length > 2) {
                            filteredValue = "${parts[0]}.${parts[1].substring(0, 2)}"
                        }
                    }
                    budgetTextFieldValue = it.copy(text = filteredValue)
                },
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
                keyboardActions = KeyboardActions(onDone = {
                    if (isConfirmEnabled) {
                        onEditBudget(budgetTextFieldValue.text.toDoubleOrNull() ?: 0.0)
                        onDismiss()
                    }
                })
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(
                onClick = {
                    if (isConfirmEnabled) {
                        onEditBudget(budgetTextFieldValue.text.toDoubleOrNull() ?: 0.0)
                        onDismiss()
                    }
                },
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

@Preview(showBackground = true)
@Composable
fun EditBudgetSheetPreview() {
    MyFinanceTheme {
        EditBudgetSheet(
            budget = 0.0,
            onDismiss = {},
            onEditBudget = {},
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
