package com.frafio.myfinance.features.labels

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.EmptyView
import com.frafio.myfinance.core.components.SwipeableSnackbarHost
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.theme.MyFinanceTheme

@Composable
fun LabelsScreen(
    appState: MyFinanceAppState,
    viewModel: LabelsViewModel,
    onBackClick: () -> Unit
) {
    val allLabels by viewModel.allLabels.collectAsStateWithLifecycle()

    LabelsContent(
        allLabels = allLabels,
        onBackClick = onBackClick,
        onAddLabel = { viewModel.addLabel(it) },
        onDeleteLabel = { viewModel.deleteLabel(it) },
        onEditLabel = { old, new -> viewModel.editLabel(old, new) },
        snackbarHost = { SwipeableSnackbarHost(hostState = appState.snackbarHostState) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelsContent(
    allLabels: List<String>,
    onBackClick: () -> Unit,
    onAddLabel: (String) -> Unit,
    onDeleteLabel: (String) -> Unit,
    onEditLabel: (String, String) -> Unit,
    snackbarHost: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    var labelFieldValue by remember { mutableStateOf(TextFieldValue(text = "")) }
    var editingLabel by remember { mutableStateOf<String?>(null) }
    var editLabelFieldValue by remember { mutableStateOf(TextFieldValue(text = "")) }
    val focusRequester = remember { FocusRequester() }

    val isLabelsEmpty = allLabels.isEmpty()

    LaunchedEffect(editingLabel) {
        if (editingLabel != null) {
            focusRequester.requestFocus()
        }
    }

    val isLabelValid = labelFieldValue.text.trim().isNotEmpty() && allLabels.none {
        it.equals(labelFieldValue.text.trim(), ignoreCase = true)
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = snackbarHost,
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                FilledTonalIconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shapes = IconButtonDefaults.shapes(),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_keyboard_arrow_left_filled),
                        contentDescription = stringResource(id = R.string.back_arrow),
                    )
                }

                Text(
                    text = stringResource(id = R.string.labels),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            NewLabelItem(
                value = labelFieldValue,
                onValueChange = { labelFieldValue = it },
                isValid = isLabelValid,
                onConfirm = {
                    onAddLabel(labelFieldValue.text.trim())
                    labelFieldValue = TextFieldValue("")
                    focusManager.clearFocus()
                },
                modifier = Modifier.padding(top = 16.dp)
            )

            if (isLabelsEmpty) {
                EmptyView(
                    modifier = Modifier.fillMaxSize(),
                    image = R.drawable.image_file_searching_rafiki,
                    message = R.string.warning_labels
                )
            } else {
                LazyColumn {
                    itemsIndexed(
                        items = allLabels,
                        key = { _, label -> label }
                    ) { index, label ->
                        LabelItem(
                            label = label,
                            allLabels = allLabels,
                            isEditing = editingLabel == label,
                            editLabelValue = editLabelFieldValue,
                            onEditLabelValueChange = { editLabelFieldValue = it },
                            onEditClick = {
                                editLabelFieldValue = TextFieldValue(
                                    text = label,
                                    selection = TextRange(label.length)
                                )
                                editingLabel = label
                            },
                            onDeleteClick = { onDeleteLabel(label) },
                            onConfirmEdit = {
                                onEditLabel(label, editLabelFieldValue.text.trim())
                                editingLabel = null
                            },
                            onCancelEdit = {
                                editingLabel = null
                                editLabelFieldValue = TextFieldValue(text = "")
                            },
                            focusRequester = focusRequester,
                            index = index,
                            count = allLabels.size
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NewLabelItem(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isValid: Boolean,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    SegmentedListItem(
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shapes = ListItemDefaults.shapes(
            shape = ListItemDefaults.shapes().selectedShape
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialShapes.Cookie12Sided.toShape())
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_filled),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        },
        content = {
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (isValid) {
                        onConfirm()
                    }
                }),
                decorationBox = { innerTextField ->
                    if (value.text.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.create_label),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            )
        },
        trailingContent = {
            Row {
                if (value.text.isNotEmpty()) {
                    IconButton(onClick = { onValueChange(TextFieldValue("")) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cancel_filled),
                            contentDescription = "Clear"
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    FilledIconButton(
                        onClick = onConfirm,
                        enabled = isValid,
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
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LabelItem(
    modifier: Modifier = Modifier,
    label: String,
    allLabels: List<String>,
    isEditing: Boolean,
    editLabelValue: TextFieldValue,
    onEditLabelValueChange: (TextFieldValue) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onConfirmEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    focusRequester: FocusRequester,
    index: Int,
    count: Int
) {
    val isLabelValid = editLabelValue.text.trim().isNotEmpty() && allLabels.none { existing ->
        if (existing == label) {
            existing == editLabelValue.text.trim()
        } else {
            existing.equals(editLabelValue.text.trim(), ignoreCase = true)
        }
    }
    SegmentedListItem(
        colors = if (isEditing)
            ListItemDefaults.colors(
                containerColor = ListItemDefaults.colors().selectedContainerColor
            )
        else
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
        shapes = if (count > 1) {
            ListItemDefaults.segmentedShapes(
                index = index,
                count = count,
                defaultShapes = ListItemDefaults.shapes(
                    shape = if (isEditing) {
                        ListItemDefaults.shapes().selectedShape
                    } else {
                        ListItemDefaults.shapes().shape
                    }
                )
            )
        } else {
            ListItemDefaults.shapes(
                shape = ListItemDefaults.shapes().selectedShape
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEditing)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isEditing)
                            R.drawable.ic_edit_outline
                        else
                            R.drawable.ic_sell_outline
                    ),
                    contentDescription = null,
                    tint = if (isEditing)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        },
        content = {
            if (isEditing) {
                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = editLabelValue,
                    onValueChange = onEditLabelValueChange,
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (isLabelValid) {
                            onConfirmEdit()
                        }
                    })
                )
            } else {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        trailingContent = {
            val firstInteractionSource = remember { MutableInteractionSource() }
            val secondInteractionSource = remember { MutableInteractionSource() }
            ButtonGroup(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                overflowIndicator = { menuState ->
                    ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                }
            ) {
                customItem(
                    {
                        FilledIconButton(
                            modifier = Modifier
                                .width(52.dp)
                                .animateWidth(firstInteractionSource),
                            onClick = if (isEditing) onCancelEdit else onEditClick,
                            shapes = IconButtonDefaults.shapes(),
                            colors = if (isEditing)
                                IconButtonDefaults.filledIconButtonColors()
                            else
                                IconButtonDefaults.filledTonalIconButtonColors(),
                            interactionSource = firstInteractionSource
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isEditing)
                                        R.drawable.ic_close_filled
                                    else
                                        R.drawable.ic_edit_outline
                                ),
                                contentDescription = stringResource(
                                    id = if (isEditing)
                                        R.string.close
                                    else
                                        R.string.edit
                                ),
                            )
                        }
                    },
                    {}
                )
                customItem(
                    {
                        FilledIconButton(
                            modifier = Modifier
                                .size(IconButtonDefaults.smallContainerSize())
                                .animateWidth(secondInteractionSource),
                            onClick = if (isEditing) onConfirmEdit else onDeleteClick,
                            enabled = !isEditing || isLabelValid,
                            shapes = IconButtonDefaults.shapes(
                                shape = IconButtonDefaults.smallSquareShape,
                            ),
                            colors = if (isEditing)
                                IconButtonDefaults.filledIconButtonColors()
                            else
                                IconButtonDefaults.filledTonalIconButtonColors(),
                            interactionSource = secondInteractionSource
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isEditing)
                                        R.drawable.ic_check_filled
                                    else
                                        R.drawable.ic_delete_outline
                                ),
                                contentDescription = stringResource(
                                    id = if (isEditing)
                                        R.string.confirm
                                    else
                                        R.string.delete
                                )
                            )
                        }
                    },
                    {}
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(
                bottom = if (index == count - 1)
                    0.dp
                else
                    2.dp
            ),
    )
}

@Preview(showBackground = true)
@Composable
fun LabelsPreview() {
    MyFinanceTheme {
        LabelsContent(
            allLabels = listOf("Food", "Transport", "Rent", "Health", "Entertainment"),
            onBackClick = {},
            onAddLabel = {},
            onDeleteLabel = {},
            onEditLabel = { _, _ -> },
            snackbarHost = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyLabelsPreview() {
    MyFinanceTheme {
        LabelsContent(
            allLabels = listOf(),
            onBackClick = {},
            onAddLabel = {},
            onDeleteLabel = {},
            onEditLabel = { _, _ -> },
            snackbarHost = {}
        )
    }
}
