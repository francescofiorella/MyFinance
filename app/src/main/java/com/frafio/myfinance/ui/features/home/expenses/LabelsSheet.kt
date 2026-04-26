package com.frafio.myfinance.ui.features.home.expenses

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.ui.components.SheetDialog
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.getCategoryIcon

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LabelsSheet(
    modifier: Modifier = Modifier,
    expense: Expense? = null,
    labels: List<String> = listOf(),
    showNewLabel: Boolean = true,
    onNewLabel: (String) -> Unit,
    onLabelCheckedChanged: (String, Boolean) -> Unit
) {

    var labelFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = "",
            )
        )
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

    SheetDialog(
        modifier = modifier,
        icon = icon,
        label = dialogLabel,
        title = title,
        labelFirst = labelFirst,
        endContent = endContent
    ) {
        val colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
        Column {
            if (showNewLabel) {
                SegmentedListItem(
                    onClick = { },
                    colors = colors,
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
                            value = labelFieldValue,
                            onValueChange = { labelFieldValue = it },
                            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Text
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                if (isLabelValid) {
                                    onNewLabel(labelFieldValue.text.trim())
                                    labelFieldValue = TextFieldValue("")
                                }
                            }),
                            decorationBox = { innerTextField ->
                                if (labelFieldValue.text.isEmpty()) {
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
                            onClick = {
                                onNewLabel(labelFieldValue.text.trim())
                                labelFieldValue = TextFieldValue("")
                            },
                            enabled = isLabelValid,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                )
            }
            labels.forEachIndexed { index, label ->
                var checked by remember(expense?.labels) {
                    mutableStateOf(
                        expense?.labels?.contains(label) ?: false
                    )
                }
                SegmentedListItem(
                    checked = checked,
                    onCheckedChange = {
                        onLabelCheckedChanged(label, it)
                        checked = it
                    },
                    colors = colors,
                    shapes = if (labels.size > 1) {
                        ListItemDefaults.segmentedShapes(
                            index = index,
                            count = labels.size,
                            defaultShapes = ListItemDefaults.shapes()
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
                                    if (checked)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.secondaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_sell_outline),
                                contentDescription = null,
                                tint = if (checked)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    },
                    content = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            /*Icon(
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(8.dp),
                                painter = painterResource(id = R.drawable.ic_more_vert),
                                contentDescription = null
                            )*/
                            Checkbox(
                                modifier = Modifier.padding(8.dp),
                                checked = checked,
                                onCheckedChange = null
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(
                            bottom = if (index == labels.size - 1)
                                0.dp
                            else
                                2.dp
                        ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LabelsSheetPreview() {
    MyFinanceTheme {
        LabelsSheet(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow),
            labels = listOf(
                "Travel",
                "Dinner"
            ),
            showNewLabel = true,
            onNewLabel = {},
            onLabelCheckedChanged = { _, _ -> }
        )
    }
}
