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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
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
    contentDescription: String,
    containerSize: Dp = IconButtonDefaults.smallContainerSize().height,
    contentSize: Dp = IconButtonDefaults.smallIconSize,
    isSelected: Boolean = false,
) {
    ImageSelectorButtonBase(
        modifier = modifier,
        onClick = onClick,
        containerSize = containerSize,
        contentSize = contentSize,
        isSelected = isSelected
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun ImageSelectorButton(
    modifier: Modifier = Modifier,
    @DrawableRes drawable: Int,
    onClick: () -> Unit,
    contentDescription: String,
    containerSize: Dp = 82.dp,
    contentSize: Dp = 32.dp,
    isSelected: Boolean = false,
) {
    ImageSelectorButtonBase(
        modifier = modifier,
        onClick = onClick,
        containerSize = containerSize,
        contentSize = contentSize,
        isSelected = isSelected
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = drawable),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop
        )
    }
}

/** The button around [image], dimmed with a check mark while selected. */
@Composable
private fun ImageSelectorButtonBase(
    modifier: Modifier,
    onClick: () -> Unit,
    containerSize: Dp,
    contentSize: Dp,
    isSelected: Boolean,
    image: @Composable () -> Unit
) {
    FilledTonalIconButton(
        modifier = modifier
            .size(containerSize)
            .semantics { selected = isSelected },
        onClick = onClick,
        enabled = !isSelected,
        shapes = IconButtonDefaults.shapes()
    ) {
        Box(contentAlignment = Alignment.Center) {
            image()

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
                onClick = {},
                contentDescription = "Avatar",
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
                drawable = R.drawable.image_profile_interface_cuate,
                onClick = {},
                contentDescription = "Avatar",
                isSelected = true
            )
        }
    }
}
