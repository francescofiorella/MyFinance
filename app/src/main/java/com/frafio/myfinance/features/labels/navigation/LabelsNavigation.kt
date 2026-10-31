package com.frafio.myfinance.features.labels.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.features.labels.LabelsScreen
import com.frafio.myfinance.features.labels.LabelsViewModel

fun EntryProviderScope<NavKey>.labelsEntry(
    onBackClick: () -> Unit,
    onManageClick: () -> Unit,
    onLabelClick: (RootKey.LabelDetail) -> Unit
) {
    entry<RootKey.Labels> {
        val viewModel: LabelsViewModel = hiltViewModel()
        LabelsScreen(
            viewModel = viewModel,
            onBackClick = onBackClick,
            onManageClick = onManageClick,
            onLabelClick = { label -> onLabelClick(RootKey.LabelDetail(label)) }
        )
    }
}
