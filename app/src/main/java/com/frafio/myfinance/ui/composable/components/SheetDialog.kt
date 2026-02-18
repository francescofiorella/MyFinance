package com.frafio.myfinance.ui.composable.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.theme.MyFinanceTheme

@Composable
fun SheetDialog(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    labelFirst: Boolean = true,
    contentHorizontalPadding: Dp = 30.dp,
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
            labelFirst = labelFirst
        )
        Box(
            modifier = Modifier.padding(horizontal = contentHorizontalPadding)
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SheetDialogPreview() {
    MyFinanceTheme {
        SheetDialog(
            icon = R.drawable.ic_person_filled,
            title = R.string.edit,
            label = R.string.your_profile,
        ) {
            Text(text = "This is the content")
        }
    }
}
