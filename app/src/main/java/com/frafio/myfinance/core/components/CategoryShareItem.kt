package com.frafio.myfinance.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.core.data.model.ExpenseCategory
import com.frafio.myfinance.core.utils.doubleToPercent
import com.frafio.myfinance.core.utils.doubleToPrice

/**
 * A category's row in a segmented group: its icon and name, the [total] with its [share] of the whole
 * (only the amount when nothing was spent), any [trailing] content, and a bar filled up to [share].
 * A [selected] row blends into the selected container color.
 */
@Composable
fun CategoryShareItem(
    category: ExpenseCategory,
    total: Double,
    share: Double,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    trailing: @Composable () -> Unit = {}
) {
    val hasSpending = total > 0.0
    val containerColor by animateColorAsState(
        targetValue = if (selected) ListItemDefaults.colors().selectedContainerColor else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "row_container"
    )
    SegmentedListItem(
        onClick = onClick,
        colors = ListItemDefaults.colors(containerColor = containerColor),
        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = count,
            defaultShapes = ListItemDefaults.shapes()
        ),
        content = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIcon(category = category)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = stringResource(id = category.nameRes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (hasSpending) {
                                "${doubleToPrice(total)} · ${doubleToPercent(share)}"
                            } else {
                                doubleToPrice(0.0)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    trailing()
                }

                ProportionBar(
                    modifier = Modifier.padding(start = 52.dp),
                    visible = hasSpending,
                    fraction = share,
                    category = category
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = if (index == count - 1) 0.dp else 2.dp)
    )
}

/**
 * A thin track filled up to [fraction] (0 to 1) in the category's color. The fill and its color animate;
 * when [visible] turns false the bar fades and collapses as its fill shrinks to zero.
 */
@Composable
private fun ProportionBar(
    modifier: Modifier = Modifier,
    visible: Boolean,
    fraction: Double,
    category: ExpenseCategory,
    animationSpec: AnimationSpec<Float> = MaterialTheme.motionScheme.slowSpatialSpec()
) {
    val isDark = isSystemInDarkTheme()
    val animatedFraction by animateFloatAsState(
        targetValue = fraction.toFloat().coerceIn(0f, 1f),
        animationSpec = animationSpec,
        label = "proportion"
    )
    // The row may show another category after a month change: blend into its color.
    val fillColor by animateColorAsState(
        targetValue = category.colors.container(isDark),
        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        label = "proportion_color"
    )
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(MaterialTheme.motionScheme.slowEffectsSpec()) +
            expandVertically(MaterialTheme.motionScheme.slowSpatialSpec()),
        exit = fadeOut(MaterialTheme.motionScheme.slowEffectsSpec()) +
            shrinkVertically(MaterialTheme.motionScheme.slowSpatialSpec())
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(fillColor)
            )
        }
    }
}
