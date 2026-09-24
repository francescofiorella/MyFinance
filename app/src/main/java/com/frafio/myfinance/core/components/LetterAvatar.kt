package com.frafio.myfinance.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.core.theme.GoogleSansFlexRoundFamily
import com.frafio.myfinance.core.theme.MyFinanceTheme

/** The first letter of [text] in a circle, for a transaction or a sheet without an icon. */
@Composable
fun LetterAvatar(
    text: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    AvatarCircle(size = size, modifier = modifier) {
        Text(
            modifier = Modifier
                .width(24.dp)
                .height(32.dp),
            text = text.firstOrNull()?.uppercaseChar()?.toString() ?: "",
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold,
            fontFamily = GoogleSansFlexRoundFamily,
            textAlign = TextAlign.Center,
            autoSize = TextAutoSize.StepBased()
        )
    }
}

@Composable
internal fun AvatarCircle(
    size: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun LetterAvatarPreview() {
    MyFinanceTheme {
        LetterAvatar(text = "Salary", size = 40.dp)
    }
}
