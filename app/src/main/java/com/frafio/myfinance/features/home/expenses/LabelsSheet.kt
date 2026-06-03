package com.frafio.myfinance.features.home.expenses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.components.AdaptiveSheet
import com.frafio.myfinance.core.components.SheetDialog
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.getCategoryIcon

@Composable
fun LabelsSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    expense: Expense? = null,
    labels: List<String> = listOf(),
    selectedLabels: List<String> = listOf(),
    showEditLabel: Boolean = true,
    onNewLabel: (String) -> Unit,
    onLabelCheckedChanged: (String, Boolean) -> Unit,
    onDeleteLabel: (String) -> Unit,
    onEditLabel: (String, String) -> Unit,
) {

    var labelFieldValue by remember(show) { mutableStateOf(TextFieldValue(text = "")) }
    var editingLabel by remember(show) { mutableStateOf<String?>(null) }
    var editLabelFieldValue by remember(show) { mutableStateOf(TextFieldValue(text = "")) }
    val focusRequester = remember(show) { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(editingLabel) {
        if (editingLabel != null) {
            focusRequester.requestFocus()
        }
    }

    val isLabelValid by remember(labelFieldValue.text) {
        derivedStateOf {
            labelFieldValue.text.isNotEmpty() && !labels.contains(labelFieldValue.text)
        }
    }

    @DrawableRes val icon = if (expense != null)
        getCategoryIcon(expense.category)
    else
        R.drawable.ic_sell_filled
    val title = expense?.name ?: stringResource(id = R.string.labels)
    val dialogLabel = expense?.getDateString() ?: stringResource(id = R.string.select)
    val labelFirst = expense == null
    val endContent = expense?.getPriceString()

    AdaptiveSheet(
        show = show,
        onDismiss = onDismiss
    ) {
        SheetDialog(
            modifier = modifier,
            icon = icon,
            label = dialogLabel,
            title = title,
            labelFirst = labelFirst,
            endContent = endContent
        ) {
            Column(modifier = Modifier.animateContentSize()) {
                if (showEditLabel) {
                    NewLabelItem(
                        value = labelFieldValue,
                        onValueChange = { labelFieldValue = it },
                        isValid = isLabelValid,
                        onConfirm = {
                            onNewLabel(labelFieldValue.text.trim())
                            labelFieldValue = TextFieldValue("")
                            focusManager.clearFocus()
                        }
                    )
                }
                labels.forEachIndexed { index, label ->
                    key(label) {
                        LabelItem(
                            label = label,
                            initialSelected = expense?.labels?.contains(label) ?: selectedLabels.contains(label),
                            isEditing = editingLabel == label,
                            editLabelValue = editLabelFieldValue,
                            onEditLabelValueChange = { editLabelFieldValue = it },
                            onLabelCheckedChanged = onLabelCheckedChanged,
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
                            showEditOptions = showEditLabel,
                            index = index,
                            count = labels.size
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NewLabelItem(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isValid: Boolean,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    SegmentedListItem(
        onClick = { },
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
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LabelItem(
    label: String,
    initialSelected: Boolean,
    isEditing: Boolean,
    editLabelValue: TextFieldValue,
    onEditLabelValueChange: (TextFieldValue) -> Unit,
    onLabelCheckedChanged: (String, Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onConfirmEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    focusRequester: FocusRequester,
    showEditOptions: Boolean,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier
) {
    var checked by remember(label, initialSelected) {
        mutableStateOf(initialSelected)
    }

    AnimatedVisibility(
        visible = true,
        enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
        exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
    ) {
        SegmentedListItem(
            checked = checked,
            onCheckedChange = {
                onLabelCheckedChanged(label, it)
                checked = it
            },
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
                            if (isEditing || checked)
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
                        tint = if (isEditing || checked)
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
                            if (editLabelValue.text.isNotEmpty() && editLabelValue.text != label) {
                                onConfirmEdit()
                            }
                        })
                    )
                } else {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            trailingContent = {
                if (isEditing) {
                    val cancelInteractionSource = remember { MutableInteractionSource() }
                    val confirmInteractionSource = remember { MutableInteractionSource() }
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
                                        .animateWidth(cancelInteractionSource),
                                    onClick = onCancelEdit,
                                    shapes = IconButtonDefaults.shapes(),
                                    interactionSource = cancelInteractionSource
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_close_filled),
                                        contentDescription = null,
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
                                        .animateWidth(confirmInteractionSource),
                                    onClick = onConfirmEdit,
                                    enabled = editLabelValue.text.isNotEmpty() && editLabelValue.text != label,
                                    shapes = IconButtonDefaults.shapes(
                                        shape = IconButtonDefaults.smallSquareShape,
                                    ),
                                    interactionSource = confirmInteractionSource
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_check_filled),
                                        contentDescription = stringResource(id = R.string.confirm)
                                    )
                                }
                            },
                            {}
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (showEditOptions) {
                            var menuExpanded by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { menuExpanded = true }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_more_vert_filled),
                                    contentDescription = null
                                )
                            }
                            DropdownMenuPopup(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuGroup(
                                    shapes = MenuDefaults.groupShapes(),
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_edit_outline),
                                                contentDescription = null
                                            )
                                        },
                                        text = { Text(text = stringResource(id = R.string.edit)) },
                                        trailingIcon = {
                                            Spacer(
                                                modifier = Modifier.width(
                                                    40.dp
                                                )
                                            )
                                        },
                                        onClick = {
                                            onEditClick()
                                            menuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_delete_outline),
                                                contentDescription = null
                                            )
                                        },
                                        text = { Text(text = stringResource(id = R.string.delete)) },
                                        trailingIcon = {
                                            Spacer(
                                                modifier = Modifier.width(
                                                    40.dp
                                                )
                                            )
                                        },
                                        onClick = {
                                            onDeleteClick()
                                            menuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Checkbox(
                            modifier = Modifier.padding(8.dp),
                            checked = checked,
                            onCheckedChange = null
                        )
                    }
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
}

@Preview(showBackground = true)
@Composable
fun LabelsSheetPreview() {
    MyFinanceTheme {
        LabelsSheet(
            show = true,
            onDismiss = {},
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow),
            labels = listOf(
                "Travel",
                "Dinner"
            ),
            showEditLabel = true,
            onNewLabel = {},
            onLabelCheckedChanged = { _, _ -> },
            onDeleteLabel = {},
            onEditLabel = { _, _ -> }
        )
    }
}
