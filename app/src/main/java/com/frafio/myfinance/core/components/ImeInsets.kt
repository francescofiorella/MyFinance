package com.frafio.myfinance.core.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity

@Composable
fun rememberImeTargetInsets(): WindowInsets {
    val density = LocalDensity.current
    val target = WindowInsets.imeAnimationTarget.getBottom(density)
    val animated by animateIntAsState(
        targetValue = target,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "imeBottom"
    )
    return WindowInsets(bottom = animated)
}
