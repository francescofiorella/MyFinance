package com.frafio.myfinance.core.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.theme.MyFinanceTheme

@Composable
fun SheetDialog(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    title: String,
    label: String,
    labelFirst: Boolean = true,
    endContent: String? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(vertical = 24.dp)
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
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    title: String,
    label: String,
    labelFirst: Boolean = true,
    endContent: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    val firstLetter =
                        title.firstOrNull()?.uppercaseChar()?.toString() ?: ""
                    Text(
                        text = firstLetter,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                val labelComposable = @Composable {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val titleComposable = @Composable {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = endContent,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        thickness = 1.dp
    )
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
                modifier = Modifier.padding(horizontal = 32.dp)
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
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}
