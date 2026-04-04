package com.frafio.myfinance.ui.home.expenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.manager.ExpensesManager.Companion.DEFAULT_LIMIT
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.utils.addTotalsToExpenses
import com.frafio.myfinance.utils.addTotalsToExpensesWithoutToday
import com.frafio.myfinance.utils.dateToUTCTimestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {
    private val expensesRepository = ExpensesRepository(
        (application as MyFinanceApplication).expensesManager
    )
    private val expensesLocalRepository = ExpensesLocalRepository()

    var listener: ExpensesListener? = null

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategories = MutableStateFlow<List<Int>>(emptyList())
    val selectedCategories = _selectedCategories.asStateFlow()

    private val _dateRange = MutableStateFlow<Pair<LocalDate, LocalDate>?>(null)
    val dateRange = _dateRange.asStateFlow()

    private val _limit = MutableStateFlow(DEFAULT_LIMIT)

    private val _scrollToId = MutableSharedFlow<String?>(replay = 0)
    val scrollToId = _scrollToId.asSharedFlow()

    val isExpensesEmpty: StateFlow<Boolean?> = expensesLocalRepository.getCount().asFlow()
        .map { it == 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: StateFlow<List<Expense>> = combine(
        _searchQuery,
        _selectedCategories,
        _dateRange
    ) { query, categories, dateRange ->
        Triple(query, categories, dateRange)
    }.flatMapLatest { (query, categories, dateRange) ->
        val effectiveCategories = categories.ifEmpty {
            listOf(
                FirestoreEnums.CATEGORIES.HOUSING.value,
                FirestoreEnums.CATEGORIES.GROCERIES.value,
                FirestoreEnums.CATEGORIES.PERSONAL_CARE.value,
                FirestoreEnums.CATEGORIES.ENTERTAINMENT.value,
                FirestoreEnums.CATEGORIES.EDUCATION.value,
                FirestoreEnums.CATEGORIES.DINING.value,
                FirestoreEnums.CATEGORIES.HEALTH.value,
                FirestoreEnums.CATEGORIES.TRANSPORTATION.value,
                FirestoreEnums.CATEGORIES.MISCELLANEOUS.value
            )
        }

        if (dateRange == null) {
            expensesLocalRepository.getWithFilter(query, effectiveCategories).asFlow()
        } else {
            expensesLocalRepository.getWithFilterDate(
                query,
                effectiveCategories,
                dateToUTCTimestamp(dateRange.first),
                dateToUTCTimestamp(dateRange.second.plusDays(1))
            ).asFlow()
        }
    }.combine(_limit) { list, limit ->
        val limitedList = list.take(limit.toInt()).map { it.copy() }
        if (_searchQuery.value.isEmpty() && _selectedCategories.value.isEmpty() && _dateRange.value == null) {
            addTotalsToExpenses(limitedList)
        } else {
            addTotalsToExpensesWithoutToday(limitedList)
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryFilterChanged(categoryId: Int) {
        val current = _selectedCategories.value.toMutableList()
        if (current.contains(categoryId)) {
            current.remove(categoryId)
        } else {
            current.add(categoryId)
        }
        _selectedCategories.value = current
    }

    fun onDateFilterChanged(dateRange: Pair<LocalDate, LocalDate>?) {
        _dateRange.value = dateRange
    }

    fun loadMore() {
        _limit.value += (DEFAULT_LIMIT / 2)
    }

    fun scrollToId(id: String) {
        viewModelScope.launch {
            _scrollToId.emit(id)
        }
    }

    fun deleteExpense(expense: Expense) {
        val response = expensesRepository.deleteExpense(expense)
        listener?.onDeleteCompleted(response, expense)
    }

    fun updateCategory(expense: Expense, newCategory: Int) {
        val updated = expense.copy(
            timestamp = dateToUTCTimestamp(expense.year!!, expense.month!!, expense.day!!),
            category = newCategory
        )
        val response = expensesRepository.editExpense(updated)
        listener?.onCompleted(response)
    }

    fun addExpense(expense: Expense) {
        val response = expensesRepository.addExpense(expense)
        listener?.onCompleted(response)
    }
}
