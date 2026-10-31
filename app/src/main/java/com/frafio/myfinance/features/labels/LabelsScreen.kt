package com.frafio.myfinance.features.labels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.AppTopBar
import com.frafio.myfinance.core.components.DetailRow
import com.frafio.myfinance.core.components.EmptyView
import com.frafio.myfinance.core.components.SectionTitle
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.dateRangeToString
import com.frafio.myfinance.core.utils.doubleToPrice
import java.time.LocalDate

@Composable
fun LabelsScreen(
    viewModel: LabelsViewModel,
    onBackClick: () -> Unit,
    onManageClick: () -> Unit,
    onLabelClick: (String) -> Unit
) {
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    LabelsContent(
        groups = groups,
        onBackClick = onBackClick,
        onManageClick = onManageClick,
        onLabelClick = onLabelClick
    )
}

/** [groups] is null while loading. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LabelsContent(
    groups: List<LabelGroup>?,
    onBackClick: () -> Unit,
    onManageClick: () -> Unit,
    onLabelClick: (String) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AppTopBar(
                title = { Text(text = stringResource(id = R.string.labels), maxLines = 1) },
                onNavigationClick = onBackClick,
                action = {
                    FilledTonalIconButton(
                        modifier = Modifier.testTag("labels_manage"),
                        onClick = onManageClick,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit_filled),
                            contentDescription = stringResource(id = R.string.manage_labels)
                        )
                    }
                }
            )

            when {
                groups == null -> Unit
                groups.isEmpty() -> EmptyView(
                    modifier = Modifier.fillMaxSize(),
                    image = R.drawable.image_file_searching_rafiki,
                    message = R.string.warning_labels
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .testTag("labels_list")
                        .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                        .fillMaxHeight()
                        .align(Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    groups.forEach { group ->
                        item(key = "group_${group.year}") {
                            SectionTitle(text = group.year?.toString() ?: stringResource(id = R.string.no_expenses))
                        }
                        itemsIndexed(group.summaries, key = { _, summary -> "label_${summary.label}" }) { index, summary ->
                            LabelRow(
                                summary = summary,
                                index = index,
                                count = group.summaries.size,
                                onClick = { onLabelClick(summary.label) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelRow(summary: LabelSummary, index: Int, count: Int, onClick: () -> Unit) {
    val first = summary.first
    val last = summary.last
    DetailRow(
        title = summary.label,
        subtitle = if (first != null && last != null) {
            dateRangeToString(first, last) + " · " +
                pluralStringResource(R.plurals.expense_count, summary.count, summary.count)
        } else {
            null
        },
        value = doubleToPrice(summary.total),
        index = index,
        count = count,
        modifier = Modifier.testTag("label_item"),
        // A label without expenses has no detail to show.
        onClick = if (summary.count > 0) onClick else null
    )
}

/** Fixed sample data for previews and screenshots. */
fun sampleLabelGroups(): List<LabelGroup> = listOf(
    LabelGroup(
        year = 2025,
        summaries = listOf(
            LabelSummary("Japan 2025", 3240.5, 18, LocalDate.of(2025, 2, 3), LocalDate.of(2025, 8, 19)),
            LabelSummary("Wedding", 860.0, 1, LocalDate.of(2025, 5, 10), LocalDate.of(2025, 5, 10)),
            LabelSummary("Home renovation", 5120.0, 23, LocalDate.of(2024, 3, 2), LocalDate.of(2025, 4, 28))
        )
    ),
    LabelGroup(
        year = 2024,
        summaries = listOf(
            LabelSummary("Christmas 2024", 420.0, 7, LocalDate.of(2024, 12, 1), LocalDate.of(2024, 12, 24)),
            LabelSummary("Lisbon 2024", 1180.4, 12, LocalDate.of(2024, 9, 12), LocalDate.of(2024, 9, 16))
        )
    ),
    LabelGroup(
        year = null,
        summaries = listOf(LabelSummary("Paris 2026", 0.0, 0, null, null))
    )
)

@Preview(showBackground = true)
@Composable
fun LabelsPreview() {
    MyFinanceTheme {
        LabelsContent(
            groups = sampleLabelGroups(),
            onBackClick = {},
            onManageClick = {},
            onLabelClick = {}
        )
    }
}
