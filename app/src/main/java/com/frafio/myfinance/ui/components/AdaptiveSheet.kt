package com.frafio.myfinance.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    val scope = rememberCoroutineScope()

    // Local state to keep the sheet in composition during animation
    var shouldRender by remember(show) { mutableStateOf(value = false) }

    LaunchedEffect(show) {
        if (show) {
            shouldRender = true
        } else {
            if (sheetState.isVisible) {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    shouldRender = false
                }
            } else {
                shouldRender = false
            }
        }
    }

    if (shouldRender) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            content = {
                content()
            },
            modifier = modifier
        )
    }
}
