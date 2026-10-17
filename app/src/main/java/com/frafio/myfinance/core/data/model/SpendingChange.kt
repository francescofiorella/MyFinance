package com.frafio.myfinance.core.data.model

/** How a month's spending compares with the month before. */
sealed interface SpendingChange {
    /** Nothing spent in either month. */
    data object None : SpendingChange

    /** Nothing spent the month before, something spent now. */
    data object New : SpendingChange

    /** Relative change, e.g. 0.12 for +12%. */
    data class Percent(val value: Double) : SpendingChange
}

fun spendingChange(current: Double, previous: Double): SpendingChange = when {
    previous > 0.0 -> SpendingChange.Percent((current - previous) / previous)
    current > 0.0 -> SpendingChange.New
    else -> SpendingChange.None
}
