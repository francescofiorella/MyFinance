package com.frafio.myfinance.core.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.frafio.myfinance.R
import com.frafio.myfinance.core.theme.MyFinanceTheme

@Composable
fun ImageSelectorButton(
    modifier: Modifier = Modifier,
    url: String,
    onClick: () -> Unit,
    contentDescription: String? = null,
    containerSize: Dp = IconButtonDefaults.smallContainerSize().height,
    contentSize: Dp = IconButtonDefaults.smallIconSize,
    isSelected: Boolean = false,
) {
    ImageSelectorButtonBase(
        modifier = modifier,
        model = url,
        isUrl = true,
        onClick = onClick,
        contentDescription = contentDescription,
        containerSize = containerSize,
        contentSize = contentSize,
        isSelected = isSelected
    )
}

@Composable
fun ImageSelectorButton(
    modifier: Modifier = Modifier,
    @DrawableRes drawable: Int,
    onClick: () -> Unit,
    contentDescription: String? = null,
    containerSize: Dp = 82.dp,
    contentSize: Dp = 32.dp,
    isSelected: Boolean = false,
) {
    ImageSelectorButtonBase(
        modifier = modifier,
        model = drawable,
        isUrl = false,
        onClick = onClick,
        contentDescription = contentDescription,
        containerSize = containerSize,
        contentSize = contentSize,
        isSelected = isSelected
    )
}

@Composable
private fun ImageSelectorButtonBase(
    modifier: Modifier = Modifier,
    model: Any,
    isUrl: Boolean,
    onClick: () -> Unit,
    contentDescription: String? = null,
    containerSize: Dp = 82.dp,
    contentSize: Dp = 32.dp,
    isSelected: Boolean = false,
) {
    FilledTonalIconButton(
        modifier = modifier.size(containerSize),
        onClick = onClick,
        enabled = !isSelected,
        shapes = IconButtonDefaults.shapes()
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isUrl) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = model,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = model as Int),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                )
                Icon(
                    modifier = Modifier.size(contentSize),
                    painter = painterResource(id = R.drawable.ic_check_filled),
                    contentDescription = null,
                    tint = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageSelectorButtonPreview() {
    MyFinanceTheme {
        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            ImageSelectorButton(
                drawable = R.drawable.image_profile_interface_cuate,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageSelectorButtonSelectedPreview() {
    MyFinanceTheme {
        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            ImageSelectorButton(
                drawable = R.drawable.image_profile_interface_pana,
                onClick = {},
                isSelected = true
            )
        }
    }
}
