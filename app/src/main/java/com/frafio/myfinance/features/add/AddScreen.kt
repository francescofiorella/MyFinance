package com.frafio.myfinance.features.add

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import com.frafio.myfinance.features.expenses.components.CategorySheet
import com.frafio.myfinance.features.expenses.components.LabelsSheet
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
    val allLabels by viewModel.allLabels.collectAsStateWithLifecycle()

    var showCategorySheet by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showLabelsSheet by rememberSaveable { mutableStateOf(false) }

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
        labels = viewModel.labels,
        onLabelClick = {
            focusManager.clearFocus()
            showLabelsSheet = true
        },
        onLabelCheckedChanged = viewModel::onLabelCheckedChanged,
        navKey = viewModel.navKey,
        onNavKeyChange = {
            focusManager.clearFocus()
            viewModel.navKey = it
            viewModel.resetErrors()
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

    LabelsSheet(
        show = showLabelsSheet,
        onDismiss = {
            if (showLabelsSheet) {
                showLabelsSheet = false
            }
        },
        labels = allLabels,
        selectedLabels = viewModel.labels,
        onNewLabel = viewModel::addLabel,
        onLabelCheckedChanged = viewModel::onLabelCheckedChanged,
        onDeleteLabel = viewModel::deleteLabel,
        onEditLabel = viewModel::editLabel
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    labels: List<String>,
    onLabelClick: () -> Unit,
    onLabelCheckedChanged: (String, Boolean) -> Unit,
    navKey: RootKey.AddEditTransaction,
    onNavKeyChange: (RootKey.AddEditTransaction) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    nameError: String? = null,
    priceError: String? = null,
    categoryError: String? = null
) {
    val focusManager = LocalFocusManager.current
    val categories = stringArrayResource(id = R.array.categories)

    var lastNonNullNameError by remember { mutableStateOf("") }
    if (nameError != null) {
        lastNonNullNameError = nameError
    }

    var isTypeSelectionVisible by remember { mutableStateOf(false) }

    val colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = onBackClick,
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
                            isTypeSelectionVisible = true
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
                                    painter = painterResource(id = R.drawable.ic_keyboard_arrow_down_filled),
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
                            contentDescription = null,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                        .align(Alignment.CenterHorizontally)
                        .verticalScroll(rememberScrollState())
                        .animateContentSize()
                ) {
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
                                // Name Field (Large)
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    TextField(
                                        value = name,
                                        onValueChange = onNameChange,
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
                                            } else if (name.isNotEmpty() && !isAdding) {
                                                IconButton(onClick = { onNameChange("") }) {
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
                                        keyboardActions = KeyboardActions(onNext = {
                                            focusManager.moveFocus(
                                                FocusDirection.Down
                                            )
                                        }),
                                        singleLine = true
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

                                // Labels Selection
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
                                            Box(
                                                modifier = Modifier
                                                    .clip(AssistChipDefaults.shape)
                                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                                    .clickable {
                                                        onLabelCheckedChanged(
                                                            label,
                                                            false
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(
                                                        horizontal = 8.dp,
                                                        vertical = 4.dp
                                                    ),
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

                                        Box(
                                            modifier = Modifier
                                                .clip(AssistChipDefaults.shape)
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .clickable { onLabelClick() },
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
                                }
                            }
                        }
                    }
                    // Amount Field
                    SegmentedListItem(
                        onClick = { },
                        shapes = ListItemDefaults.segmentedShapes(
                            index = 0,
                            count = if (navKey.expenseCode == AddViewModel.REQUEST_EXPENSE_CODE) 3 else 2,
                            defaultShapes = ListItemDefaults.shapes()
                        ),
                        colors = colors,
                        content = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val currency = stringResource(id = R.string.currency)
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 6.dp)
                                        .size(48.dp)
                                        .clip(MaterialShapes.Cookie7Sided.toShape())
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = when (currency) {
                                                "$" -> R.drawable.ic_attach_money_filled
                                                "€" -> R.drawable.ic_euro_filled
                                                else -> R.drawable.ic_euro_filled
                                            }
                                        ),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
                                            } else if (priceString.isNotEmpty() && !isAdding) {
                                                IconButton(onClick = { onPriceChange("") }) {
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
                                        keyboardActions = KeyboardActions(onDone = {
                                            focusManager.clearFocus()
                                            if (!isAdding) onSaveClick()
                                        }),
                                        singleLine = true
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
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 2.dp)
                    )

                    // Date Field
                    SegmentedListItem(
                        onClick = if (!isAdding) onDateClick else {
                            { }
                        },
                        shapes = ListItemDefaults.segmentedShapes(
                            index = 1,
                            count = if (navKey.expenseCode == AddViewModel.REQUEST_EXPENSE_CODE) 3 else 2,
                            defaultShapes = ListItemDefaults.shapes()
                        ),
                        colors = colors,
                        content = {
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
                            onClick = if (!isAdding) onCategoryClick else {
                                { }
                            },
                            shapes = ListItemDefaults.segmentedShapes(
                                index = 2,
                                count = if (navKey.expenseCode == AddViewModel.REQUEST_EXPENSE_CODE) 3 else 2,
                                defaultShapes = ListItemDefaults.shapes()
                            ),
                            colors = colors,
                            content = {
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
                                                id = if (category != -1) getCategoryIcon(
                                                    category
                                                ) else R.drawable.ic_grid_3x3_filled
                                            ),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        TextField(
                                            value = if (category != -1) categories.getOrElse(
                                                category
                                            ) { "" } else "",
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
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp)) // plus 2.dp from card
                }
            }
        }

        // Type Selection
        AnimatedVisibility(
            visible = isTypeSelectionVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isTypeSelectionVisible = false },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = {
                        focusManager.clearFocus()
                        onNavKeyChange(navKey.copy(expenseCode = AddViewModel.REQUEST_EXPENSE_CODE))
                        isTypeSelectionVisible = false
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
                        onNavKeyChange(navKey.copy(expenseCode = AddViewModel.REQUEST_INCOME_CODE))
                        isTypeSelectionVisible = false
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

