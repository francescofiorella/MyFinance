package com.frafio.myfinance.ui.home.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.manager.IncomesManager.Companion.DEFAULT_LIMIT
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.repository.IncomeRepository
import com.frafio.myfinance.data.repository.IncomesLocalRepository
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.data.storage.MyFinanceStorage
import com.frafio.myfinance.utils.addTotalsToIncomes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val expensesRepository: ExpensesRepository,
    private val incomeRepository: IncomeRepository,
    incomesLocalRepository: IncomesLocalRepository
) : ViewModel() {

    var listener: BudgetListener? = null

    private val _limit = MutableStateFlow(DEFAULT_LIMIT)

    private val _scrollToId = MutableSharedFlow<String?>(replay = 1)
    val scrollToId = _scrollToId.asSharedFlow()

    val monthlyBudget: StateFlow<Double> = MyFinanceStorage.monthlyBudget.asFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val annualBudget: StateFlow<Double> = monthlyBudget
        .map { it * 12 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val incomes: StateFlow<List<Income>> = incomesLocalRepository.getAll().asFlow()
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
        setMonthlyBudget(0.0, true)
    }

    fun setMonthlyBudget(budget: Double, getOldBudget: Boolean = false, notify: Boolean = true) {
        listener?.onStarted(notify)
        val previousBudget = if (getOldBudget) monthlyBudget.value else null
        val response = expensesRepository.setMonthlyBudget(budget)
        listener?.onCompleted(response, previousBudget, notify)
    }

    fun deleteIncome(income: Income) {
        listener?.onStarted()
        val response = incomeRepository.deleteIncome(income)
        listener?.onDeleteCompleted(response, income)
    }

    fun addIncome(income: Income, notify: Boolean = true) {
        listener?.onStarted(notify)
        val response = incomeRepository.addIncome(income)
        listener?.onCompleted(response, null, notify)
    }
}
