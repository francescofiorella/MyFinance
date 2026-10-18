package com.frafio.myfinance.core.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.theme.MyFinanceTheme

/**
 * The top bar of every sub-screen: a navigation button at the start, a centered [title] and an
 * optional [action] at the end. [title] gets `titleLarge` and `onSurface`, so a plain `Text` is enough.
 */
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    onNavigationClick: () -> Unit,
    @DrawableRes navigationIcon: Int = R.drawable.ic_keyboard_arrow_left_filled,
    navigationContentDescription: String = stringResource(id = R.string.navigate_up),
    navigationModifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        FilledTonalIconButton(
            modifier = navigationModifier.align(Alignment.CenterStart),
            onClick = onNavigationClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shapes = IconButtonDefaults.shapes(),
        ) {
            Icon(
                painter = painterResource(id = navigationIcon),
                contentDescription = navigationContentDescription,
            )
        }

        Box(modifier = Modifier.padding(horizontal = 56.dp)) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                ProvideTextStyle(value = MaterialTheme.typography.titleLarge, content = title)
            }
        }

        if (action != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                action()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppTopBarPreview() {
    MyFinanceTheme {
        AppTopBar(title = { Text(text = "Labels") }, onNavigationClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun AppTopBarWithActionPreview() {
    MyFinanceTheme {
        AppTopBar(
            title = { Text(text = "Password") },
            onNavigationClick = {},
            action = {
                FilledIconButton(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .width(52.dp),
                    onClick = {},
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_filled),
                        contentDescription = stringResource(R.string.save),
                    )
                }
            }
        )
    }
}
