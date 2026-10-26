package com.frafio.myfinance.core.utils

import com.frafio.myfinance.core.data.model.DatePoint
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Months shown by each range of a monthly chart, in button order; null is Auto: as many as fit the width. */
val MONTH_RANGE_COUNTS: List<Int?> = listOf(null, 6, 12, 24)

/** Every range, as indices into [MONTH_RANGE_COUNTS]. */
val ALL_MONTH_RANGES: List<Int> = MONTH_RANGE_COUNTS.indices.toList()

/** How many months the range at [index] shows, given how many bars fit the chart's width. */
fun monthRangeCount(index: Int, fittingBars: Int): Int = MONTH_RANGE_COUNTS[index] ?: fittingBars

/** How many months to load so that every range can be shown: the longest one, or more if more fit the width. */
fun monthsToLoad(fittingBars: Int): Int = maxOf(fittingBars, MONTH_RANGE_COUNTS.maxOf { it ?: 0 })

/**
 * The first month with data on a monthly chart ending with [lastMonth]: the month of the [earliest] expense,
 * but never after [lastMonth], which always has data even when nothing was spent.
 */
fun trackingStart(earliest: DatePoint?, lastMonth: LocalDate): LocalDate {
    val start = earliest?.let { LocalDate.of(it.year, it.month, 1) } ?: return lastMonth
    return minOf(start, lastMonth)
}

/** Months from [trackingStart] to [lastMonth], both included; at least one. */
fun trackedMonths(trackingStart: LocalDate, lastMonth: LocalDate): Int =
    (ChronoUnit.MONTHS.between(trackingStart, lastMonth) + 1).toInt().coerceAtLeast(1)

/**
 * The ranges worth offering for [trackedMonths] months of data, as indices into [MONTH_RANGE_COUNTS]: Auto,
 * plus each fixed range while the shorter one leaves tracked months out. Only Auto when it already shows
 * them all and 6M would add nothing, so no buttons are needed.
 */
fun availableMonthRanges(trackedMonths: Int, fittingBars: Int): List<Int> {
    val ranges = ALL_MONTH_RANGES.filter { index ->
        MONTH_RANGE_COUNTS[index] == null || trackedMonths > (MONTH_RANGE_COUNTS.getOrNull(index - 1) ?: 0)
    }
    return if (ranges.size == 2 && fittingBars >= trackedMonths) listOf(0) else ranges
}

/** The range shown when [selected] is chosen: itself while [available], Auto otherwise. */
fun shownMonthRange(selected: Int, available: List<Int>): Int = if (selected in available) selected else 0
