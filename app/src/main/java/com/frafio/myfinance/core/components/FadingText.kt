package com.frafio.myfinance.core.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

/**
 * A [Text] that cross-fades to the new [text] when it changes, like the charts' centre and header.
 * Both texts take [color], so an animated color blends on the old text too while it fades out.
 */
@Composable
fun FadingText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true
) {
    val fadeSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    AnimatedContent(
        targetState = text,
        modifier = modifier,
        transitionSpec = { fadeIn(fadeSpec) togetherWith fadeOut(fadeSpec) using SizeTransform(clip = false) },
        contentAlignment = when (textAlign) {
            TextAlign.Center -> Alignment.TopCenter
            TextAlign.End, TextAlign.Right -> Alignment.TopEnd
            else -> Alignment.TopStart
        },
        label = "fading_text"
    ) { shown ->
        Text(
            text = shown,
            color = color,
            style = style,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            softWrap = softWrap
        )
    }
}
