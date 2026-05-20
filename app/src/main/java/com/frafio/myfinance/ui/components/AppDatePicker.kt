package com.frafio.myfinance.ui.components

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
import com.frafio.myfinance.utils.toUTCLocalDateTime
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

        DatePickerDialog(
            modifier = modifier,
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateSelected(it.toUTCLocalDateTime().toLocalDate())
                        }
                        onDismiss()
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.cancel))
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

        DatePickerDialog(
            modifier = modifier,
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis?.toUTCLocalDateTime()?.toLocalDate()
                        val end = dateRangePickerState.selectedEndDateMillis?.toUTCLocalDateTime()?.toLocalDate()
                        if (start != null && end != null) {
                            onRangeSelected(start, end)
                        }
                        onDismiss()
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.cancel))
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
