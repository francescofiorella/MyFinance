package com.frafio.myfinance.core.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ShareBarDefaults {
    val Height: Dp = 12.dp
    val Gap: Dp = 2.dp

    val AnimationEasing: Easing = LinearOutSlowInEasing

    const val BarAnimationDuration: Int = 1000

    /** How the arcs grow and shrink when the values change; pass `snap()` for a static chart. */
    val AnimationSpec: AnimationSpec<Float> = tween(BarAnimationDuration, easing = AnimationEasing)
}

data class ShareBarItem(
    val value: Double,
    val color: Color
)

/** Each value's fraction of the width left after the gaps. */
internal fun shareFractions(values: List<Double>): List<Float> {
    val sum = values.sum()
    return values.map { if (sum > 0) (it / sum).toFloat() else 0f }
}

/** A gap follows every positive segment that has another positive one after it. */
internal fun gapsAfter(values: List<Double>): List<Boolean> =
    values.mapIndexed { index, value -> value > 0.0 && values.drop(index + 1).any { it > 0.0 } }

/**
 * One bar split into a segment per item, sized by its share of the total. Segments grow
 * and shrink in place, fade out at zero, and an empty track fades in when the total is zero.
 */
@Composable
fun ShareBar(
    modifier: Modifier = Modifier,
    items: List<ShareBarItem>,
    animationSpec: AnimationSpec<Float> = ShareBarDefaults.AnimationSpec,
    height: Dp = ShareBarDefaults.Height,
    gap: Dp = ShareBarDefaults.Gap,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
) {
    val gapPx = with(LocalDensity.current) { gap.toPx() }

    val values = items.map { it.value }
    val fractions = remember(values) { shareFractions(values) }
    val gaps = remember(values) { gapsAfter(values) }

    val animatedFractions = fractions.mapIndexed { index, fraction ->
        animateFloatAsState(targetValue = fraction, animationSpec = animationSpec, label = "share_$index")
    }
    val animatedGaps = gaps.mapIndexed { index, hasGap ->
        animateFloatAsState(targetValue = if (hasGap) gapPx else 0f, animationSpec = animationSpec, label = "gap_$index")
    }
    val animatedAlphas = fractions.mapIndexed { index, fraction ->
        animateFloatAsState(targetValue = if (fraction > 0f) 1f else 0f, animationSpec = animationSpec, label = "alpha_$index")
    }
    val emptyAlpha by animateFloatAsState(
        targetValue = if (values.sum() == 0.0) 1f else 0f,
        animationSpec = animationSpec,
        label = "empty_alpha"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
    ) {
        if (emptyAlpha > 0.001f) {
            drawRect(color = emptyColor, alpha = emptyAlpha)
        }
        val available = size.width - animatedGaps.sumOf { it.value.toDouble() }.toFloat()
        var x = 0f
        items.indices.forEach { index ->
            val width = animatedFractions[index].value * available
            if (fractions[index] > 0f || animatedFractions[index].value > 0.001f) {
                drawRect(
                    color = items[index].color,
                    topLeft = Offset(x, 0f),
                    size = Size(width, size.height),
                    alpha = animatedAlphas[index].value
                )
            }
            x += width + animatedGaps[index].value
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShareBarPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ShareBar(
                items = listOf(Color(0xFF6750A4), Color(0xFF00897B), Color(0xFFF9A825), Color(0xFFD32F2F))
                    .mapIndexed { index, color -> ShareBarItem(value = (index + 1) * 10.0, color = color) },
                animationSpec = snap()
            )
        }
    }
}
