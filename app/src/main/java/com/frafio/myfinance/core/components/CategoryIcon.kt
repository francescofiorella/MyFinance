package com.frafio.myfinance.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.data.model.ExpenseCategory
import com.frafio.myfinance.core.theme.MyFinanceTheme

object CategoryIconDefaults {
    /** How the colors blend when the category changes; pass `snap()` to switch at once. */
    val ColorAnimationSpec: AnimationSpec<Color>
        @Composable get() = MaterialTheme.motionScheme.slowEffectsSpec()
}

/**
 * The category's icon in a circle of its colors, or in neutral colors when [colored] is false. When
 * [category] changes, the colors blend; the icon itself switches at once.
 */
@Composable
fun CategoryIcon(
    modifier: Modifier = Modifier,
    category: ExpenseCategory,
    colored: Boolean = true,
    size: Dp = IconButtonDefaults.smallContainerSize().height,
    iconSize: Dp = IconButtonDefaults.smallIconSize,
    shape: Shape = CircleShape,
    colorAnimationSpec: AnimationSpec<Color> = CategoryIconDefaults.ColorAnimationSpec
) {
    val isDark = isSystemInDarkTheme()
    val containerColor by animateColorAsState(
        targetValue = if (colored) category.colors.container(isDark) else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = colorAnimationSpec,
        label = "icon_container"
    )
    val contentColor by animateColorAsState(
        targetValue = if (colored) category.colors.onContainer(isDark) else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = colorAnimationSpec,
        label = "icon_content"
    )
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = category.iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryIconPreview() {
    MyFinanceTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            CategoryIcon(category = ExpenseCategories.all.first())
        }
    }
}
