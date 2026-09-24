package com.frafio.myfinance.core.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.utils.toUTCLocalDateTime
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    initialDate: LocalDate? = null,
    title: String = stringResource(id = R.string.select),
) {
    if (show) {
        val initialSelectedDateMillis = initialDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialSelectedDateMillis)
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }

        AppPickerDialog(
            modifier = modifier,
            onDismiss = onDismiss,
            confirmEnabled = confirmEnabled.value,
            onConfirm = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(it.toUTCLocalDateTime().toLocalDate())
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = title,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDateRangePickerDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onRangeSelected: (LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    initialStartDate: LocalDate? = null,
    initialEndDate: LocalDate? = null,
    title: String = stringResource(id = R.string.select),
) {
    if (show) {
        val initialSelectedStartDateMillis = initialStartDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val initialSelectedEndDateMillis = initialEndDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = initialSelectedStartDateMillis,
            initialSelectedEndDateMillis = initialSelectedEndDateMillis
        )
        val confirmEnabled = remember {
            derivedStateOf {
                dateRangePickerState.selectedStartDateMillis != null &&
                        dateRangePickerState.selectedEndDateMillis != null
            }
        }

        AppPickerDialog(
            modifier = modifier,
            onDismiss = onDismiss,
            confirmEnabled = confirmEnabled.value,
            onConfirm = {
                val start = dateRangePickerState.selectedStartDateMillis?.toUTCLocalDateTime()?.toLocalDate()
                val end = dateRangePickerState.selectedEndDateMillis?.toUTCLocalDateTime()?.toLocalDate()
                if (start != null && end != null) {
                    onRangeSelected(start, end)
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text = title,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                },
                showModeToggle = false
            )
        }
    }
}

/** The dialog both pickers share: OK runs [onConfirm] and closes, Cancel closes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppPickerDialog(
    modifier: Modifier,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                enabled = confirmEnabled
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        },
        content = content
    )
}
