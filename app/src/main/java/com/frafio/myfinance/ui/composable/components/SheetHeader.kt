package com.frafio.myfinance.ui.composable.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
fun SheetHeader(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    labelFirst: Boolean = true
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
            Column {
                val labelText = @Composable {
                    Text(
                        text = stringResource(id = label),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    )
                }
                val titleText = @Composable {
                    Text(
                        text = stringResource(id = title),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 22.sp
                        )
                    )
                }

                if (labelFirst) {
                    labelText()
                    titleText()
                } else {
                    titleText()
                    labelText()
                }
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
fun SheetHeaderPreview() {
    MyFinanceTheme {
        SheetHeader(
            icon = R.drawable.ic_person_filled,
            title = R.string.your_profile,
            label = R.string.edit
        )
    }
}
