package com.frafio.myfinance.core.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
    imageResLight: Int?,
    imageResDark: Int?,
    messageRes: Int
) {
    val isDarkTheme = isSystemInDarkTheme()
    val configuration = LocalConfiguration.current

    if (imageResLight == null || imageResDark == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        return
    }

    val imageRes = if (isDarkTheme) imageResDark else imageResLight

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        Row(
            modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .aspectRatio(1f)
            )
            Spacer(modifier = Modifier.width(32.dp))
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.widthIn(max = 400.dp)
            )
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyViewPreview() {
    MyFinanceTheme {
        EmptyView(
            imageResLight = R.drawable.image_audit_pana,
            imageResDark = R.drawable.image_shared_goals_amico,
            messageRes = R.string.warning_home
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun EmptyViewLandscapePreview() {
    MyFinanceTheme {
        EmptyView(
            imageResLight = R.drawable.image_audit_pana,
            imageResDark = R.drawable.image_shared_goals_amico,
            messageRes = R.string.warning_home
        )
    }
}


@Preview(showBackground = true)
@Composable
fun EmptyViewNoImagePreview() {
    MyFinanceTheme {
        EmptyView(
            imageResLight = null,
            imageResDark = null,
            messageRes = R.string.warning_home
        )
    }
}