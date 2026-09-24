package com.frafio.myfinance.core.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/** The surface a swipe dismisses; wider than the snackbar's text. */
const val SWIPEABLE_SNACKBAR_TAG = "swipeable_snackbar"

@OptIn(ExperimentalMaterial3Api::class)
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
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(40.dp)
                )
            }
        )
    }
}