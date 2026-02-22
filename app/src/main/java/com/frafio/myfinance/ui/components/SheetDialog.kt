package com.frafio.myfinance.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.theme.MyFinanceTheme

@Composable
fun SheetDialog(
    @DrawableRes icon: Int,
    title: String,
    label: String,
    modifier: Modifier = Modifier,
    labelFirst: Boolean = true,
    endContent: String? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 30.dp)
    ) {
        SheetHeader(
            icon = icon,
            title = title,
            label = label,
            labelFirst = labelFirst,
            endContent = endContent
        )
        content()
    }
}

@Composable
private fun SheetHeader(
    @DrawableRes icon: Int,
    title: String,
    label: String,
    modifier: Modifier = Modifier,
    labelFirst: Boolean = true,
    endContent: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = { },
                enabled = false,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                val labelComposable = @Composable {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
                val titleComposable = @Composable {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp
                    )
                }

                if (labelFirst) {
                    labelComposable()
                    titleComposable()
                } else {
                    titleComposable()
                    labelComposable()
                }
            }
            if (endContent != null) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = endContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 18.sp
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp, horizontal = 16.dp),
            thickness = 1.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SheetDialogPreview() {
    MyFinanceTheme {
        SheetDialog(
            icon = R.drawable.ic_person_filled,
            title = stringResource(id = R.string.edit),
            label = stringResource(id = R.string.your_profile),
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Text(
                text = "This is the content",
                modifier = Modifier.padding(horizontal = 30.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseSheetDialogPreview() {
    MyFinanceTheme {
        SheetDialog(
            icon = R.drawable.ic_home_filled,
            title = stringResource(id = R.string.expense),
            label = "01/01/1970",
            labelFirst = false,
            endContent = "€ 0.00",
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Text(
                text = "This is the content",
                modifier = Modifier.padding(horizontal = 30.dp)
            )
        }
    }
}
