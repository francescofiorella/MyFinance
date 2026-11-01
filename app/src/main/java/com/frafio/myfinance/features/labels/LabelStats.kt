package com.frafio.myfinance.features.labels

import com.frafio.myfinance.core.data.model.Expense
import java.time.LocalDate

data class LabelSummary(
    val label: String,
    val total: Double,
    val count: Int,
    /** The days of the label's first and last expense; null without expenses. */
    val first: LocalDate?,
    val last: LocalDate?
)

/** Labels whose last expense falls in [year]; a null [year] holds the labels without expenses. */
data class LabelGroup(
    val year: Int?,
    val summaries: List<LabelSummary>
)

/**
 * The [labels] grouped by the year of their last expense, newest first, each group most recent first;
 * the labels without expenses come last. An expense counts toward each of its labels, and labels not in
 * [labels] are ignored.
 */
fun buildLabelGroups(labels: List<String>, expenses: List<Expense>): List<LabelGroup> {
    val byLabel = expenses
        .flatMap { expense -> expense.labels.distinct().map { label -> label to expense } }
        .groupBy({ it.first }, { it.second })
    return labels.asSequence().distinct()
        .map { label ->
            val labelled = byLabel[label].orEmpty()
            val dates = labelled.map { it.getLocalDate() }
            LabelSummary(label, labelled.sumOf { it.price }, labelled.size, dates.minOrNull(), dates.maxOrNull())
        }
        .sortedWith(compareBy<LabelSummary, LocalDate?>(nullsLast(reverseOrder())) { it.last }.thenBy { it.label })
        .groupBy { it.last?.year }
        .map { (year, summaries) -> LabelGroup(year, summaries) }.toList()
}
