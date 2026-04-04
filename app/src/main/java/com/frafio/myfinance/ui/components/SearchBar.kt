package com.frafio.myfinance.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.theme.MyFinanceTheme

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.search)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search_filled),
                        contentDescription = null
                    )
                },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close_filled),
                                contentDescription = null
                            )
                        }
                    }
                } else null,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true
            )
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_filter_list_filled),
                    contentDescription = stringResource(R.string.filter)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    MyFinanceTheme {
        SearchBar(
            query = "Pizza",
            onQueryChange = {},
            onFilterClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
