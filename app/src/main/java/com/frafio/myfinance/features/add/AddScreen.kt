package com.frafio.myfinance.features.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.AppDatePickerDialog
import com.frafio.myfinance.core.components.SwipeableSnackbarHost
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.getCategoryIcon
import com.frafio.myfinance.features.home.expenses.CategorySheet
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    appState: MyFinanceAppState,
    viewModel: AddViewModel,
    onBackClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    val isAdding by viewModel.isAdding.collectAsStateWithLifecycle()

    var showCategorySheet by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    AddScreen(
        appState = appState,
        isAdding = isAdding,
        name = viewModel.name,
        onNameChange = {
            viewModel.name = it
            viewModel.nameError = null
        },
        priceString = viewModel.priceString,
        onPriceChange = {
            viewModel.priceString = it
            viewModel.priceError = null
        },
        dateString = viewModel.dateString ?: "",
        onDateClick = {
            focusManager.clearFocus()
            showDatePicker = true
        },
        category = viewModel.category,
        onCategoryClick = {
            focusManager.clearFocus()
            showCategorySheet = true
        },
        navKey = viewModel.navKey,
        onNavKeyChange = {
            focusManager.clearFocus()
            viewModel.navKey = it
        },
        onSaveClick = {
            focusManager.clearFocus()
            viewModel.onAddButtonClick()
        },
        onBackClick = onBackClick,
        nameError = viewModel.nameError,
        priceError = viewModel.priceError,
        categoryError = viewModel.categoryError
    )

    CategorySheet(
        show = showCategorySheet,
        onDismiss = {
            if (showCategorySheet) {
                showCategorySheet = false
            }
        },
        onCategorySelected = {
            viewModel.category = it
            viewModel.categoryError = null
            if (showCategorySheet) {
                showCategorySheet = false
            }
        }
    )

    AppDatePickerDialog(
        show = showDatePicker,
        onDismiss = {
            if (showDatePicker) {
                showDatePicker = false
            }
        },
        onDateSelected = {
            viewModel.year = it.year
            viewModel.month = it.monthValue
            viewModel.day = it.dayOfMonth
            if (showDatePicker) {
                showDatePicker = false
            }
        },
        initialDate = LocalDate.of(
            viewModel.year,
            viewModel.month,
            viewModel.day
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    appState: MyFinanceAppState,
    isAdding: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    priceString: String,
    onPriceChange: (String) -> Unit,
    dateString: String,
    onDateClick: () -> Unit,
    category: Int,
    onCategoryClick: () -> Unit,
    navKey: RootKey.AddEditTransaction,
    onNavKeyChange: (RootKey.AddEditTransaction) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    showCategoryField: Boolean = navKey.expenseCode == AddViewModel.REQUEST_EXPENSE_CODE,
    nameError: String? = null,
    priceError: String? = null,
    categoryError: String? = null
) {
    val categories = stringArrayResource(id = R.array.categories)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            snackbarHost = {
                SwipeableSnackbarHost(hostState = appState.snackbarHostState)
            },
            floatingActionButton = {
                CompositionLocalProvider(
                    LocalRippleConfiguration provides
                            if (isAdding) null else LocalRippleConfiguration.current
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (!isAdding) onSaveClick()
                        },
                        containerColor = if (!isAdding) {
                            ButtonDefaults.filledTonalButtonColors().containerColor
                        } else {
                            ButtonDefaults.filledTonalButtonColors().disabledContainerColor
                        },
                        contentColor = if (!isAdding) {
                            ButtonDefaults.filledTonalButtonColors().contentColor
                        } else {
                            ButtonDefaults.filledTonalButtonColors().disabledContentColor
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check_filled),
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(id = R.string.save)
                            )
                        }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                FilledTonalIconButton(
                    onClick = onBackClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .padding(start = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_filled),
                        contentDescription = stringResource(id = R.string.back_arrow),
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Name Field (Large)
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = name,
                        onValueChange = onNameChange,
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.name),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 44.dp, end = 8.dp),
                        enabled = !isAdding,
                        textStyle = MaterialTheme.typography.headlineMedium,
                        isError = nameError != null,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            autoCorrectEnabled = false
                        ),
                        singleLine = true
                    )
                    if (nameError != null) {
                        Text(
                            text = nameError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 60.dp)
                        )
                    }
                }

                // Type Selection (Chips)
                Row(
                    modifier = Modifier
                        .padding(start = 60.dp)
                        .offset(y = (-8).dp)
                ) {
                    FilterChip(
                        selected = navKey.expenseCode == AddViewModel.REQUEST_EXPENSE_CODE,
                        onClick = { onNavKeyChange(navKey.copy(expenseCode = AddViewModel.REQUEST_EXPENSE_CODE)) },
                        label = {
                            Text(
                                text = stringResource(id = R.string.expense)
                            )
                        },
                        enabled = navKey.requestCode == AddViewModel.REQUEST_ADD_CODE && !isAdding
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = navKey.expenseCode == AddViewModel.REQUEST_INCOME_CODE,
                        onClick = { onNavKeyChange(navKey.copy(expenseCode = AddViewModel.REQUEST_INCOME_CODE)) },
                        label = {
                            Text(
                                text = stringResource(id = R.string.income)
                            )
                        },
                        enabled = navKey.requestCode == AddViewModel.REQUEST_ADD_CODE && !isAdding
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Amount Field
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = priceString,
                        onValueChange = { text ->
                            if (text.isEmpty() || text.toDoubleOrNull() != null || text == ".") {
                                onPriceChange(text)
                            }
                        },
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.amount),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 18.sp
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        enabled = !isAdding,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp
                        ),
                        isError = priceError != null,
                        leadingIcon = {
                            val currency = stringResource(id = R.string.currency)
                            Icon(
                                painter = painterResource(
                                    id = when (currency) {
                                        "$" -> R.drawable.ic_attach_money_filled
                                        "€" -> R.drawable.ic_euro_filled
                                        else -> R.drawable.ic_euro_filled
                                    }
                                ),
                                contentDescription = null
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    if (priceError != null) {
                        Text(
                            text = priceError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 56.dp)
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Date Field
                Box(
                    modifier = if (!isAdding) {
                        Modifier
                            .fillMaxWidth()
                            .clickable { onDateClick() }
                    } else {
                        Modifier
                            .fillMaxWidth()
                    }
                ) {
                    TextField(
                        value = dateString,
                        onValueChange = {},
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.date),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 18.sp
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp
                        ),
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_today_filled),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = if (isAdding) {
                            TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent
                            )
                        } else {
                            TextFieldDefaults.colors(
                                disabledContainerColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }

                // Category Field
                if (showCategoryField) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Box(
                        modifier = if (!isAdding) {
                            Modifier
                                .fillMaxWidth()
                                .clickable { onCategoryClick() }
                        } else {
                            Modifier
                                .fillMaxWidth()
                        }
                    ) {
                        TextField(
                            value = if (category != -1) categories.getOrElse(category) { "" } else "",
                            onValueChange = {},
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.category),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 18.sp
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 18.sp
                            ),
                            isError = categoryError != null,
                            enabled = false,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(
                                        id = if (category != -1) getCategoryIcon(category) else R.drawable.ic_grid_3x3_filled
                                    ),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = if (isAdding) {
                                TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent
                                )
                            } else {
                                TextFieldDefaults.colors(
                                    disabledContainerColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        if (categoryError != null) {
                            Text(
                                text = categoryError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 56.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp)) // dashboard_bottom_margin
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddScreenPreview() {
    MyFinanceTheme {
        val appState = rememberMyFinanceAppState()
        AddScreen(
            appState = appState,
            isAdding = false,
            name = "Test Expense",
            onNameChange = {},
            priceString = "10.0",
            onPriceChange = {},
            dateString = "29 May 2026",
            onDateClick = {},
            category = 1,
            onCategoryClick = {},
            navKey = RootKey.AddEditTransaction(
                requestCode = AddViewModel.REQUEST_ADD_CODE,
                expenseCode = AddViewModel.REQUEST_EXPENSE_CODE
            ),
            onNavKeyChange = {},
            onSaveClick = {},
            onBackClick = {},
            nameError = null,
            priceError = null,
            categoryError = null
        )
    }
}
