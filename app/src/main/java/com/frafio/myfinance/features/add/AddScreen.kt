package com.frafio.myfinance.features.add

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.AppDatePickerDialog
import com.frafio.myfinance.core.components.SwipeableSnackbarHost
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.getCategoryIcon
import com.frafio.myfinance.core.utils.getCurrencyIcon
import com.frafio.myfinance.features.expenses.components.CategorySheet
import com.frafio.myfinance.features.expenses.components.LabelsSheet
import java.time.LocalDate
import androidx.compose.foundation.text.input.TextFieldLineLimits
import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    appState: MyFinanceAppState,
    viewModel: AddViewModel,
    onBackClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    val isAdding by viewModel.isAdding.collectAsStateWithLifecycle()
    val allLabels by viewModel.allLabels.collectAsStateWithLifecycle()
    val currencyCode by viewModel.currencyCode.collectAsStateWithLifecycle()

    val nameState = rememberTextFieldState(viewModel.name)
    val priceState = rememberTextFieldState(viewModel.priceString)

    var showCategorySheet by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showLabelsSheet by rememberSaveable { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(nameState.text) {
        nameError = null
    }

    LaunchedEffect(priceState.text) {
        priceError = null
    }

    AddScreen(
        appState = appState,
        isAdding = isAdding,
        nameState = nameState,
        priceState = priceState,
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
        labels = viewModel.labels,
        onLabelClick = {
            focusManager.clearFocus()
            showLabelsSheet = true
        },
        onLabelCheckedChanged = viewModel::onLabelCheckedChanged,
        navKey = viewModel.navKey,
        onNavKeyChange = { newNavKey ->
            focusManager.clearFocus()
            viewModel.navKey = newNavKey
            nameError = null
            priceError = null
            categoryError = null
        },
        onSaveClick = {
            focusManager.clearFocus()

            val name = nameState.text.toString().trim()
            val priceString = priceState.text.toString().trim()
            var hasError = false

            if (name.isEmpty()) {
                nameError = FinanceCode.EMPTY_NAME.message
                hasError = true
            } else if (name == FirestoreEnums.NAMES.TOTAL.valueEn ||
                name == FirestoreEnums.NAMES.TOTAL.valueIt) {
                nameError = FinanceCode.WRONG_NAME_TOTAL.message
                hasError = true
            }

            if (priceString.isEmpty()) {
                priceError = FinanceCode.EMPTY_AMOUNT.message
                hasError = true
            } else if (priceString.toDoubleOrNull() == null || priceString.toDouble() == 0.0) {
                priceError = FinanceCode.WRONG_AMOUNT.message
                hasError = true
            }

            if (viewModel.navKey.expenseCode != AddViewModel.REQUEST_INCOME_CODE && viewModel.category == -1) {
                categoryError = FinanceCode.EMPTY_CATEGORY.message
                hasError = true
            }

            if (!hasError) {
                viewModel.onAddButtonClick(
                    name = name,
                    priceString = priceString,
                    category = viewModel.category,
                    year = viewModel.year,
                    month = viewModel.month,
                    day = viewModel.day,
                    labels = viewModel.labels
                )
            }
        },
        onBackClick = onBackClick,
        nameError = nameError,
        priceError = priceError,
        categoryError = categoryError,
        currencyCode = currencyCode
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
            categoryError = null
            if (showCategorySheet) {
                showCategorySheet = false
            }
        }
    )

    LabelsSheet(
        show = showLabelsSheet,
        onDismiss = {
            if (showLabelsSheet) {
                showLabelsSheet = false
            }
        },
        labels = allLabels,
        selectedLabels = viewModel.labels,
        onLabelCheckedChanged = viewModel::onLabelCheckedChanged
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
    nameState: TextFieldState,
    priceState: TextFieldState,
    dateString: String,
    onDateClick: () -> Unit,
    category: Int,
    onCategoryClick: () -> Unit,
    labels: List<String>,
    onLabelClick: () -> Unit,
    onLabelCheckedChanged: (String, Boolean) -> Unit,
    navKey: RootKey.AddEditTransaction,
    onNavKeyChange: (RootKey.AddEditTransaction) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    nameError: String? = null,
    priceError: String? = null,
    categoryError: String? = null,
    currencyCode: String = "EUR"
) {
    var isTypeSelectionVisible by remember { mutableStateOf(false) }

    BackHandler(enabled = isTypeSelectionVisible) {
        isTypeSelectionVisible = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.blur(if (isTypeSelectionVisible) 16.dp else 0.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            snackbarHost = {
                SwipeableSnackbarHost(hostState = appState.snackbarHostState)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AddTopBar(
                    navKey = navKey,
                    isAdding = isAdding,
                    isTypeSelectionVisible = isTypeSelectionVisible,
                    onBackClick = onBackClick,
                    onToggleTypeSelection = { isTypeSelectionVisible = it },
                    onSaveClick = onSaveClick
                )

                Column(
                    modifier = Modifier
                        .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                        .align(Alignment.CenterHorizontally)
                        .verticalScroll(rememberScrollState())
                        .animateContentSize()
                ) {
                    NameAndLabelsCard(
                        nameState = nameState,
                        nameError = nameError,
                        isAdding = isAdding,
                        navKey = navKey,
                        labels = labels,
                        onLabelClick = onLabelClick,
                        onLabelCheckedChanged = onLabelCheckedChanged
                    )

                    TransactionForm(
                        priceState = priceState,
                        priceError = priceError,
                        dateString = dateString,
                        onDateClick = onDateClick,
                        category = category,
                        onCategoryClick = onCategoryClick,
                        categoryError = categoryError,
                        isAdding = isAdding,
                        navKey = navKey,
                        onSaveClick = onSaveClick,
                        currencyCode = currencyCode
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        TransactionTypeOverlay(
            isVisible = isTypeSelectionVisible,
            onDismiss = { isTypeSelectionVisible = false },
            onTypeSelected = { type ->
                onNavKeyChange(navKey.copy(expenseCode = type))
                isTypeSelectionVisible = false
            }
        )
    }
}

@Composable
fun AddTopBar(
    navKey: RootKey.AddEditTransaction,
    isAdding: Boolean,
    isTypeSelectionVisible: Boolean,
    onBackClick: () -> Unit,
    onToggleTypeSelection: (Boolean) -> Unit,
    onSaveClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(
            onClick = {
                if (isTypeSelectionVisible) {
                    onToggleTypeSelection(false)
                } else {
                    onBackClick()
                }
            },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shapes = IconButtonDefaults.shapes(),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close_filled),
                contentDescription = stringResource(id = R.string.back_arrow),
            )
        }

        TextButton(
            onClick = {
                focusManager.clearFocus()
                onToggleTypeSelection(true)
            },
            colors = ButtonDefaults.textButtonColors(
                contentColor = TextFieldDefaults.colors().focusedTextColor,
                disabledContentColor = if (isAdding) {
                    TextFieldDefaults.colors().disabledTextColor
                } else {
                    TextFieldDefaults.colors().focusedTextColor
                }
            ),
            enabled = navKey.requestCode == AddViewModel.REQUEST_ADD_CODE && !isAdding
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        id = if (navKey.expenseCode == AddViewModel.REQUEST_EXPENSE_CODE) {
                            R.string.expense
                        } else {
                            R.string.income
                        }
                    ),
                    style = MaterialTheme.typography.titleLarge,
                )
                if (navKey.requestCode == AddViewModel.REQUEST_ADD_CODE) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 4.dp),
                        painter = painterResource(
                            id = if (isTypeSelectionVisible)
                                R.drawable.ic_keyboard_arrow_up_filled
                            else
                                R.drawable.ic_keyboard_arrow_down_filled
                        ),
                        contentDescription = null,
                    )
                }
            }
        }

        FilledIconButton(
            modifier = Modifier
                .padding(end = 4.dp)
                .width(52.dp),
            onClick = onSaveClick,
            enabled = !isAdding,
            shapes = IconButtonDefaults.shapes()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_filled),
                contentDescription = stringResource(R.string.save),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NameAndLabelsCard(
    nameState: TextFieldState,
    nameError: String?,
    isAdding: Boolean,
    navKey: RootKey.AddEditTransaction,
    labels: List<String>,
    onLabelClick: () -> Unit,
    onLabelCheckedChanged: (String, Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var lastNonNullNameError by remember { mutableStateOf("") }
    if (nameError != null) {
        lastNonNullNameError = nameError
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .animateContentSize(),
        shape = ListItemDefaults.shapes().selectedShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .padding(vertical = 16.dp)
                    .size(64.dp)
                    .clip(MaterialShapes.Cookie12Sided.toShape())
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit_filled),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        state = nameState,
                        placeholder = {
                            Text(
                                text = stringResource(
                                    id = if (navKey.expenseCode == AddViewModel.REQUEST_EXPENSE_CODE)
                                        R.string.expense_name
                                    else
                                        R.string.income_name
                                ),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        enabled = !isAdding,
                        textStyle = MaterialTheme.typography.headlineSmall,
                        trailingIcon = {
                            if (nameError != null) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_error_filled),
                                    contentDescription = null
                                )
                            } else if (nameState.text.isNotEmpty() && !isAdding) {
                                IconButton(onClick = { nameState.edit { replace(0, length, "") } }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_cancel_filled),
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        },
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
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Next
                        ),
                        onKeyboardAction = {
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                        lineLimits = TextFieldLineLimits.SingleLine
                    )
                    if (nameError != null) {
                        Text(
                            text = lastNonNullNameError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 16.dp)
                        )
                    }
                }
// ...

                AnimatedVisibility(
                    visible = navKey.expenseCode == AddViewModel.REQUEST_EXPENSE_CODE,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        labels.forEach { label ->
                            LabelChip(
                                label = label,
                                onClick = { onLabelCheckedChanged(label, false) }
                            )
                        }

                        AddLabelChip(onClick = onLabelClick)
                    }
                }
            }
        }
    }
}

@Composable
fun LabelChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(AssistChipDefaults.shape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(id = R.drawable.ic_close_filled),
                contentDescription = "Remove Label",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun AddLabelChip(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(AssistChipDefaults.shape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .size(AssistChipDefaults.IconSize),
            painter = painterResource(id = R.drawable.ic_add_filled),
            contentDescription = "Add Label",
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionForm(
    priceState: TextFieldState,
    priceError: String?,
    dateString: String,
    onDateClick: () -> Unit,
    category: Int,
    onCategoryClick: () -> Unit,
    categoryError: String?,
    isAdding: Boolean,
    navKey: RootKey.AddEditTransaction,
    onSaveClick: () -> Unit,
    currencyCode: String
) {
    val colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    val categories = stringArrayResource(id = R.array.categories)

    Column {
        // Amount Field
        SegmentedListItem(
            shapes = ListItemDefaults.segmentedShapes(
                index = 0,
                count = if (navKey.expenseCode == AddViewModel.REQUEST_EXPENSE_CODE) 3 else 2,
                defaultShapes = ListItemDefaults.shapes()
            ),
            colors = colors,
            content = {
                AmountField(
                    priceState = priceState,
                    priceError = priceError,
                    isAdding = isAdding,
                    onSaveClick = onSaveClick,
                    currencyCode = currencyCode
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 2.dp)
        )
// ...

        // Date Field
        SegmentedListItem(
            onClick = if (!isAdding) onDateClick else { {} },
            shapes = ListItemDefaults.segmentedShapes(
                index = 1,
                count = if (navKey.expenseCode == AddViewModel.REQUEST_EXPENSE_CODE) 3 else 2,
                defaultShapes = ListItemDefaults.shapes()
            ),
            colors = colors,
            content = {
                DateField(
                    dateString = dateString,
                    isAdding = isAdding
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 2.dp)
        )

        // Category Field
        AnimatedVisibility(
            visible = navKey.expenseCode == AddViewModel.REQUEST_EXPENSE_CODE,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            SegmentedListItem(
                onClick = if (!isAdding) onCategoryClick else { {} },
                shapes = ListItemDefaults.segmentedShapes(
                    index = 2,
                    count = 3,
                    defaultShapes = ListItemDefaults.shapes()
                ),
                colors = colors,
                content = {
                    CategoryField(
                        category = category,
                        categoryError = categoryError,
                        categories = categories,
                        isAdding = isAdding
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AmountField(
    priceState: TextFieldState,
    priceError: String?,
    isAdding: Boolean,
    onSaveClick: () -> Unit,
    currencyCode: String
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .size(48.dp)
                .clip(MaterialShapes.Cookie7Sided.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = getCurrencyIcon(currencyCode)),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            TextField(
                state = priceState,
                inputTransformation = {
                    val newText = asCharSequence().toString()
                    if (newText.isNotEmpty() && newText.toDoubleOrNull() == null && newText != ".") {
                        revertAllChanges()
                    }
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.amount),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAdding,
                textStyle = MaterialTheme.typography.bodyLarge,
                trailingIcon = {
                    if (priceError != null) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_error_filled),
                            contentDescription = null
                        )
                    } else if (priceState.text.isNotEmpty() && !isAdding) {
                        IconButton(onClick = { priceState.edit { replace(0, length, "") } }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cancel_filled),
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                isError = priceError != null,
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
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                onKeyboardAction = {
                    focusManager.clearFocus()
                    if (!isAdding) onSaveClick()
                },
                lineLimits = TextFieldLineLimits.SingleLine
            )
            if (priceError != null) {
                Text(
                    text = priceError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DateField(
    dateString: String,
    isAdding: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .size(48.dp)
                .clip(MaterialShapes.Pill.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_today_filled),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextField(
            value = dateString,
            onValueChange = {},
            placeholder = {
                Text(
                    text = stringResource(id = R.string.date),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            enabled = false,
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
            },
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CategoryField(
    category: Int,
    categoryError: String?,
    categories: Array<String>,
    isAdding: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .size(48.dp)
                .clip(MaterialShapes.Sunny.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = if (category != -1) getCategoryIcon(category)
                    else R.drawable.ic_grid_3x3_filled
                ),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = if (category != -1) categories.getOrElse(category) { "" } else "",
                onValueChange = {},
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.category),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                isError = categoryError != null,
                enabled = false,
                trailingIcon = {
                    if (categoryError != null) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_error_filled),
                            contentDescription = null
                        )
                    }
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
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.error
                    )
                },
                singleLine = true
            )
            if (categoryError != null) {
                Text(
                    text = categoryError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun TransactionTypeOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onTypeSelected: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.scrim.copy(
                        alpha = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) 0.7f else 0.1f
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    onTypeSelected(AddViewModel.REQUEST_EXPENSE_CODE)
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TextFieldDefaults.colors().focusedTextColor
                )
            ) {
                Text(
                    text = stringResource(R.string.expense),
                    style = MaterialTheme.typography.headlineMedium
                    )
            }
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    onTypeSelected(AddViewModel.REQUEST_INCOME_CODE)
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TextFieldDefaults.colors().focusedTextColor
                )
            ) {
                Text(
                    text = stringResource(R.string.income),
                    style = MaterialTheme.typography.headlineMedium
                )
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
            nameState = rememberTextFieldState("Test Expense"),
            priceState = rememberTextFieldState("10.0"),
            dateString = "29 May 2026",
            onDateClick = {},
            category = 1,
            onCategoryClick = {},
            labels = listOf("Dinner", "Cheat Meal"),
            onLabelClick = {},
            onLabelCheckedChanged = { _, _ -> },
            navKey = RootKey.AddEditTransaction(
                requestCode = AddViewModel.REQUEST_ADD_CODE,
                expenseCode = AddViewModel.REQUEST_EXPENSE_CODE
            ),
            onNavKeyChange = {},
            onSaveClick = {},
            onBackClick = {},
            nameError = "Error message",
            priceError = "Error message",
            categoryError = "Error message"
        )
    }
}
