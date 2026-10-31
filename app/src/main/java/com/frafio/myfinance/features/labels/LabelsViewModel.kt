package com.frafio.myfinance.features.labels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.di.Dispatcher
import com.frafio.myfinance.core.di.MyFinanceDispatchers.Default
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LabelsViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    expensesLocalRepository: ExpensesLocalRepository,
    @Dispatcher(Default) defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    /** Null until loaded. */
    val groups: StateFlow<List<LabelGroup>?> = combine(
        userPreferencesRepository.userPreferencesFlow.map { it.labels },
        expensesLocalRepository.getLabelled(),
        ::buildLabelGroups
    )
        .flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
