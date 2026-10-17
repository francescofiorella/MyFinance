package com.frafio.myfinance.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.data.model.ExpenseCategory
import com.frafio.myfinance.core.theme.MyFinanceTheme

/** The category's icon in a circle of its colors, or in neutral colors when [colored] is false. */
@Composable
fun CategoryIcon(
    modifier: Modifier = Modifier,
    category: ExpenseCategory,
    colored: Boolean = true,
    size: Dp = IconButtonDefaults.smallContainerSize().height,
    iconSize: Dp = IconButtonDefaults.smallIconSize,
    shape: Shape = CircleShape
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                if (colored) category.colors.container(isDark) else MaterialTheme.colorScheme.surfaceContainerHighest
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = category.iconRes),
            contentDescription = null,
            tint = if (colored) category.colors.onContainer(isDark) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryIconPreview() {
    MyFinanceTheme {
        CategoryIcon(category = ExpenseCategories.all.first())
    }
}
