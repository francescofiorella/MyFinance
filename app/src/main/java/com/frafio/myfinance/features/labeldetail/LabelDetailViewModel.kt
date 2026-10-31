package com.frafio.myfinance.features.labeldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.di.Dispatcher
import com.frafio.myfinance.core.di.MyFinanceDispatchers.Default
import com.frafio.myfinance.core.navigation.RootKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface LabelDetailUiState {
    data object Loading : LabelDetailUiState
    data object Empty : LabelDetailUiState
    data class Loaded(val stats: LabelDetailStats) : LabelDetailUiState
}

@HiltViewModel(assistedFactory = LabelDetailViewModel.Factory::class)
class LabelDetailViewModel @AssistedInject constructor(
    expensesLocalRepository: ExpensesLocalRepository,
    @Dispatcher(Default) defaultDispatcher: CoroutineDispatcher,
    @Assisted navKey: RootKey.LabelDetail
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: RootKey.LabelDetail): LabelDetailViewModel
    }

    val label: String = navKey.label

    val uiState: StateFlow<LabelDetailUiState> = expensesLocalRepository.getLabelled()
        .map { expenses ->
            buildLabelDetail(expenses.filter { label in it.labels })
                ?.let { LabelDetailUiState.Loaded(it) }
                ?: LabelDetailUiState.Empty
        }
        .flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LabelDetailUiState.Loading)
}
