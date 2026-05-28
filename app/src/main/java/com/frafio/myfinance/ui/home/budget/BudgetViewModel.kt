package com.frafio.myfinance.ui.home.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.manager.IncomesManager.Companion.DEFAULT_LIMIT
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.data.repository.IncomeRepository
import com.frafio.myfinance.data.repository.IncomesLocalRepository
import com.frafio.myfinance.data.repository.LoadingRepository
import com.frafio.myfinance.data.repository.UserPreferencesRepository
import com.frafio.myfinance.utils.addTotalsToIncomes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BudgetUiEvent {

    data class ShowSnackBar(
        val message: String,
        val actionText: String? = null,
        val actionFun: () -> Unit = {},
        val dismissFun: () -> Unit = {}
    ) : BudgetUiEvent()
    data class BudgetUpdated(val previousBudget: Double) : BudgetUiEvent()
    data class IncomeDeleted(val income: Income) : BudgetUiEvent()
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val expensesRepository: ExpensesRepository,
    private val incomeRepository: IncomeRepository,
    incomesLocalRepository: IncomesLocalRepository,
    userPreferencesRepository: UserPreferencesRepository,
    private val loadingRepository: LoadingRepository
) : ViewModel() {

    private val _limit = MutableStateFlow(DEFAULT_LIMIT)

    private val _scrollToId = MutableSharedFlow<String?>(replay = 1)
    val scrollToId = _scrollToId.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<BudgetUiEvent>()
    val uiEvents: SharedFlow<BudgetUiEvent> = _uiEvents.asSharedFlow()

    val monthlyBudget: StateFlow<Double> = userPreferencesRepository.userPreferencesFlow
        .map { it.monthlyBudget }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val annualBudget: StateFlow<Double> = monthlyBudget
        .map { it * 12 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val incomes: StateFlow<List<Income>> = incomesLocalRepository.getAll()
        .combine(_limit) { list, limit ->
            list.take(limit.toInt())
        }
        .map { limitedList ->
            addTotalsToIncomes(limitedList)
        }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isIncomesEmpty: StateFlow<Boolean> = incomes
        .map { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val itemMetadata: StateFlow<Map<Int, Pair<Int, Int>>> = incomes
        .map { list ->
            val result = mutableMapOf<Int, Pair<Int, Int>>()
            var start = 0
            while (start < list.size) {
                val income = list[start]
                if (income.category == FirestoreEnums.CATEGORIES.TOTAL.value ||
                    income.category == FirestoreEnums.CATEGORIES.JOLLY.value
                ) {
                    result[start] = Pair(0, 1)
                    start++
                } else {
                    val year = income.year
                    var end = start
                    while (end < list.size &&
                        list[end].category != FirestoreEnums.CATEGORIES.TOTAL.value &&
                        list[end].category != FirestoreEnums.CATEGORIES.JOLLY.value &&
                        list[end].year == year
                    ) {
                        end++
                    }
                    val count = end - start
                    for (i in 0 until count) {
                        result[start + i] = Pair(i, count)
                    }
                    start = end
                }
            }
            result
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun loadMore() {
        _limit.value += DEFAULT_LIMIT
    }

    fun scrollToId(id: String?) {
        viewModelScope.launch {
            _scrollToId.emit(id)
        }
    }

    fun deleteMonthlyBudget() {
        setMonthlyBudget(0.0)
    }

    fun setMonthlyBudget(budget: Double, notify: Boolean = true) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val previousBudget = monthlyBudget.value
                val response = expensesRepository.setMonthlyBudget(budget)
                if (notify) {
                    if (response.code == FinanceCode.BUDGET_UPDATE_SUCCESS.code) {
                        _uiEvents.emit(BudgetUiEvent.BudgetUpdated(previousBudget))
                    } else {
                        _uiEvents.emit(BudgetUiEvent.ShowSnackBar(response.message))
                    }
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val response = incomeRepository.deleteIncome(income)
                if (response.code == FinanceCode.INCOME_DELETE_SUCCESS.code) {
                    _uiEvents.emit(BudgetUiEvent.IncomeDeleted(income))
                } else {
                    _uiEvents.emit(BudgetUiEvent.ShowSnackBar(response.message))
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun addIncome(income: Income, notify: Boolean = true) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val response = incomeRepository.addIncome(income)
                if (notify) {
                    _uiEvents.emit(BudgetUiEvent.ShowSnackBar(response.message))
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }
}
