package com.frafio.myfinance.core.components

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.theme.MyFinanceTheme

@Composable
fun EmptyView(
    modifier: Modifier = Modifier,
    @DrawableRes image: Int? = null,
    @DrawableRes imageDark: Int? = null,
    @StringRes message: Int,
    contentAlignment: Alignment = Alignment.Center
) {
    val imageRes = if (isSystemInDarkTheme()) imageDark ?: image else image
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = modifier,
        contentAlignment = contentAlignment
    ) {
        when {
            imageRes == null -> EmptyMessage(message = message, textAlign = TextAlign.Center)
            isLandscape -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                EmptyImage(imageRes = imageRes, widthFraction = 0.3f)
                Spacer(modifier = Modifier.width(32.dp))
                EmptyMessage(
                    message = message,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.widthIn(max = 400.dp)
                )
            }
            else -> Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EmptyImage(imageRes = imageRes, widthFraction = 0.6f)
                Spacer(modifier = Modifier.height(24.dp))
                EmptyMessage(
                    message = message,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyImage(@DrawableRes imageRes: Int, widthFraction: Float) {
    Image(
        painter = painterResource(imageRes),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .aspectRatio(1f)
    )
}

@Composable
private fun EmptyMessage(
    @StringRes message: Int,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(message),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        textAlign = textAlign,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun EmptyViewNoImagePreview() {
    MyFinanceTheme {
        EmptyView(
            modifier = Modifier.fillMaxSize(),
            image = null,
            imageDark = null,
            message = R.string.warning_home,
            contentAlignment = Alignment.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyViewPreview() {
    MyFinanceTheme {
        EmptyView(
            modifier = Modifier.fillMaxSize(),
            image = R.drawable.image_consulting_cuate,
            imageDark = R.drawable.image_investment_data_cuate,
            message = R.string.warning_home,
            contentAlignment = Alignment.Center
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun EmptyViewLandscapePreview() {
    MyFinanceTheme {
        EmptyView(
            modifier = Modifier.fillMaxSize(),
            image = R.drawable.image_consulting_cuate,
            imageDark = R.drawable.image_investment_data_cuate,
            message = R.string.warning_home,
            contentAlignment = Alignment.Center
        )
    }
}
