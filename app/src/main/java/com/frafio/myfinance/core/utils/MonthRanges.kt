package com.frafio.myfinance.core.utils

import com.frafio.myfinance.core.data.model.DatePoint
import java.time.LocalDate

/** Months shown by each range of a monthly chart, in button order; null is Auto: as many as fit the width. */
val MONTH_RANGE_COUNTS: List<Int?> = listOf(null, 6, 12, 24)

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
