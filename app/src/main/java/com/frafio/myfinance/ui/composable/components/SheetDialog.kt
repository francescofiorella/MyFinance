package com.frafio.myfinance.ui.composable.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
