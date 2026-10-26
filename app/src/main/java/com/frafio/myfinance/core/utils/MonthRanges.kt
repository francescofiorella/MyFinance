package com.frafio.myfinance.core.utils

/** Months shown by each range of a monthly chart, in button order; null is Auto: as many as fit the width. */
val MONTH_RANGE_COUNTS: List<Int?> = listOf(null, 6, 12, 24)

/** How many months the range at [index] shows, given how many bars fit the chart's width. */
fun monthRangeCount(index: Int, fittingBars: Int): Int = MONTH_RANGE_COUNTS[index] ?: fittingBars

/** How many months to load so that every range can be shown: the longest one, or more if more fit the width. */
fun monthsToLoad(fittingBars: Int): Int = maxOf(fittingBars, MONTH_RANGE_COUNTS.maxOf { it ?: 0 })
