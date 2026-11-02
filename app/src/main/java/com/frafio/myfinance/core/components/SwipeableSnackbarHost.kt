package com.frafio.myfinance.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/** The surface a swipe dismisses; wider than the snackbar's text. */
const val SWIPEABLE_SNACKBAR_TAG = "swipeable_snackbar"
const val SNACKBAR_IMAGE_TAG = "snackbar_image"

/** A message led by a round picture, such as the user's profile picture. */
class ImageSnackbarVisuals(
    override val message: String,
    val image: @Composable () -> Painter,
    override val duration: SnackbarDuration = SnackbarDuration.Short
) : SnackbarVisuals {
    override val actionLabel: String? = null
    override val withDismissAction: Boolean = false
}

@Composable
fun SwipeableSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState = hostState) { data ->
        val dismissState = rememberSwipeToDismissBoxState()
        LaunchedEffect(dismissState.currentValue) {
            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                data.dismiss()
            }
        }
        SwipeToDismissBox(
            modifier = Modifier.testTag(SWIPEABLE_SNACKBAR_TAG),
            state = dismissState,
            backgroundContent = {},
            content = {
                val visuals = data.visuals
                if (visuals is ImageSnackbarVisuals) {
                    // The same margin Snackbar(snackbarData) applies to itself.
                    Snackbar(modifier = Modifier.padding(12.dp), shape = RoundedCornerShape(40.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = visuals.image(),
                                contentDescription = null,
                                modifier = Modifier
                                    .testTag(SNACKBAR_IMAGE_TAG)
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = visuals.message)
                        }
                    }
                } else {
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(40.dp)
                    )
                }
            }
        )
    }
}
