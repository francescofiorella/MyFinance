package com.frafio.myfinance.features.labeldetail

import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.features.categorydetail.FrequentExpense
import com.frafio.myfinance.features.categorydetail.TOP_EXPENSES_COUNT
import com.frafio.myfinance.features.categorydetail.frequentExpenses
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/** Most slots a timeline shows, gaps included: a month of days. */
const val MAX_TIMELINE_SLOTS = 31

/** Most gaps a timeline shows: a sparse label moves to a coarser unit rather than showing many breaks. */
const val MAX_TIMELINE_GAPS = 3

data class DayTotal(
    val date: LocalDate,
    val total: Double
)

/** Statistics over a label's whole lifetime. */
data class LabelDetailStats(
    val total: Double,
    val count: Int,
    val averagePerExpense: Double,
    val first: LocalDate,
    val last: LocalDate,
    /** Days from [first] to [last], both included. */
    val spanDays: Int,
    /** Days with at least one expense. */
    val activeDays: Int,
    val averagePerActiveDay: Double,
    /** The day with the highest total; the most recent one on a tie. */
    val biggestDay: DayTotal,
    val timeline: LabelTimeline,
    val topExpenses: List<Expense>,
    val frequent: List<FrequentExpense>
)

enum class TimelineUnit {
    DAY, WEEK, MONTH, YEAR;

    /** The first day of the unit holding [date]; weeks start on Monday. */
    fun startOf(date: LocalDate): LocalDate = when (this) {
        DAY -> date
        WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        MONTH -> date.withDayOfMonth(1)
        YEAR -> date.withDayOfYear(1)
    }

    fun plus(start: LocalDate, units: Long): LocalDate = when (this) {
        DAY -> start.plusDays(units)
        WEEK -> start.plusWeeks(units)
        MONTH -> start.plusMonths(units)
        YEAR -> start.plusYears(units)
    }

    /** A chart key growing by one per unit; months match `MonthlyTotal.toBarChartEntry`. */
    fun keyOf(start: LocalDate): Int = when (this) {
        DAY -> start.toEpochDay().toInt()
        WEEK -> Math.floorDiv(start.toEpochDay(), 7L).toInt()
        MONTH -> start.year * 12 + start.monthValue - 1
        YEAR -> start.year
    }

    /** Empty units in a row that collapse into a gap; years never do. */
    internal val gapThreshold: Int?
        get() = when (this) {
            DAY -> 7
            WEEK -> 4
            MONTH -> 3
            YEAR -> null
        }
}

sealed interface TimelineSlot {
    val start: LocalDate

    /** What was spent in the unit starting on [start]; zero when nothing was. */
    data class Bucket(override val start: LocalDate, val total: Double) : TimelineSlot

    /** [units] empty units in a row, from [start], collapsed into one slot. */
    data class Gap(override val start: LocalDate, val units: Int) : TimelineSlot
}

data class LabelTimeline(
    val unit: TimelineUnit,
    val slots: List<TimelineSlot>
)

enum class GapUnit { DAYS, WEEKS, MONTHS, YEARS }

data class GapLength(
    val amount: Int,
    val unit: GapUnit
)

/** Statistics of a label's [expenses], newest first; null without expenses. */
fun buildLabelDetail(expenses: List<Expense>): LabelDetailStats? {
    if (expenses.isEmpty()) return null
    val total = expenses.sumOf { it.price }
    val days = expenses
        .groupBy { it.getLocalDate() }
        .map { (date, dayExpenses) -> DayTotal(date, dayExpenses.sumOf { it.price }) }
    val first = days.minOf { it.date }
    val last = days.maxOf { it.date }
    return LabelDetailStats(
        total = total,
        count = expenses.size,
        averagePerExpense = total / expenses.size,
        first = first,
        last = last,
        spanDays = ChronoUnit.DAYS.between(first, last).toInt() + 1,
        activeDays = days.size,
        averagePerActiveDay = total / days.size,
        biggestDay = days.maxWith(compareBy<DayTotal> { it.total }.thenBy { it.date }),
        timeline = buildLabelTimeline(expenses),
        topExpenses = expenses.sortedByDescending { it.price }.take(TOP_EXPENSES_COUNT),
        frequent = frequentExpenses(expenses)
    )
}

/**
 * The spending of [expenses] over time, in the finest unit that keeps it within [MAX_TIMELINE_SLOTS] slots
 * and [MAX_TIMELINE_GAPS] gaps; years always fit.
 */
fun buildLabelTimeline(expenses: List<Expense>): LabelTimeline =
    TimelineUnit.entries.asSequence()
        .map { unit -> LabelTimeline(unit, timelineSlots(expenses, unit)) }
        .first { timeline ->
            timeline.unit == TimelineUnit.YEAR ||
                (timeline.slots.size <= MAX_TIMELINE_SLOTS && timeline.slots.count { it is TimelineSlot.Gap } <= MAX_TIMELINE_GAPS)
        }

/**
 * One bucket per [unit] from the first expense to the last; a run of empty units reaching the unit's
 * threshold becomes a single gap, shorter runs stay as zero buckets.
 */
internal fun timelineSlots(expenses: List<Expense>, unit: TimelineUnit): List<TimelineSlot> {
    if (expenses.isEmpty()) return emptyList()
    val totals = expenses
        .groupBy { unit.startOf(it.getLocalDate()) }
        .mapValues { (_, unitExpenses) -> unitExpenses.sumOf { it.price } }
    val last = totals.keys.max()
    val slots = mutableListOf<TimelineSlot>()
    val emptyRun = mutableListOf<LocalDate>()
    var current = totals.keys.min()
    while (!current.isAfter(last)) {
        val total = totals[current]
        if (total == null) {
            emptyRun += current
        } else {
            val threshold = unit.gapThreshold
            if (threshold != null && emptyRun.size >= threshold) {
                slots += TimelineSlot.Gap(emptyRun.first(), emptyRun.size)
            } else {
                emptyRun.mapTo(slots) { TimelineSlot.Bucket(it, 0.0) }
            }
            emptyRun.clear()
            slots += TimelineSlot.Bucket(current, total)
        }
        current = unit.plus(current, 1)
    }
    return slots
}

/** How long [gap] of [unit]s lasts, in what reads best: days under two weeks, weeks under two months, then months, then years from two. */
fun gapLength(gap: TimelineSlot.Gap, unit: TimelineUnit): GapLength {
    val end = unit.plus(gap.start, gap.units.toLong())
    val days = ChronoUnit.DAYS.between(gap.start, end).toInt()
    val months = Period.between(gap.start, end).toTotalMonths().toInt()
    return when {
        days < 14 -> GapLength(days, GapUnit.DAYS)
        days < 60 -> GapLength(Math.round(days / 7.0).toInt(), GapUnit.WEEKS)
        months < 24 -> GapLength(months, GapUnit.MONTHS)
        else -> GapLength(months / 12, GapUnit.YEARS)
    }
}
