package com.frafio.charts.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** How far a press pulse grows before a release may shrink it, as in ButtonGroup: even a quick tap shows it. */
private const val PressPulsePeak = 0.75f

/** Animates to 1 while [interactions] hold a press and back to 0 after, only once past [PressPulsePeak], like ButtonGroup. */
internal suspend fun Animatable<Float, AnimationVector1D>.followPresses(
    interactions: Flow<Interaction>,
    spec: AnimationSpec<Float>
) = coroutineScope {
    val presses = mutableListOf<PressInteraction.Press>()
    interactions
        .map { interaction ->
            when (interaction) {
                is PressInteraction.Press -> presses.add(interaction)
                is PressInteraction.Release -> presses.remove(interaction.press)
                is PressInteraction.Cancel -> presses.remove(interaction.press)
            }
            presses.isNotEmpty()
        }
        .distinctUntilChanged()
        .collectLatest { pressed ->
            if (pressed) {
                this@coroutineScope.launch { animateTo(1f, spec) }
            } else {
                snapshotFlow { value }.first { it > PressPulsePeak }
                animateTo(0f, spec)
            }
        }
}
