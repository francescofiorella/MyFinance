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
import com.frafio.myfinance.data.storage.MyFinanceStorage
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
import kotlinx.coroutines.flow.distinctUntilChanged
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

    private val _scrollToId = MutableSharedFlow<String?>(replay = 1)
    val scrollToId = _scrollToId.asSharedFlow()

    val labels: StateFlow<List<String>> = MyFinanceStorage.labels.asFlow()
        .map { it.sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        list.take(limit.toInt())
    }.map { limitedList ->
        if (_searchQuery.value.isEmpty() && _selectedCategories.value.isEmpty() && _dateRange.value == null) {
            addTotalsToExpenses(limitedList)
        } else {
            addTotalsToExpensesWithoutToday(limitedList)
        }
    }.flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val itemMetadata: StateFlow<Map<Int, Pair<Int, Int>>> = expenses
        .map { list ->
            val result = mutableMapOf<Int, Pair<Int, Int>>()
            var start = 0
            while (start < list.size) {
                val expense = list[start]
                if (expense.category == FirestoreEnums.CATEGORIES.TOTAL.value ||
                    expense.category == FirestoreEnums.CATEGORIES.JOLLY.value
                ) {
                    result[start] = Pair(0, 1)
                    start++
                } else {
                    val date = expense.getLocalDate()
                    var end = start
                    while (end < list.size &&
                        list[end].category != FirestoreEnums.CATEGORIES.TOTAL.value &&
                        list[end].category != FirestoreEnums.CATEGORIES.JOLLY.value &&
                        list[end].getLocalDate() == date
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
        _limit.value += (DEFAULT_LIMIT)
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

    fun addLabel(label: String) {
        val currentLabels = labels.value

        if (currentLabels.contains(label)) return

        val response = expensesRepository.setLabels(currentLabels + label)
        listener?.onCompleted(response)
    }

    fun addLabelToExpense(expense: Expense, label: String) {
        if (expense.labels.contains(label)) return
        val labels = expense.labels.toMutableList()
        labels.add(label)
        val updated = expense.copy(
            timestamp = dateToUTCTimestamp(expense.year!!, expense.month!!, expense.day!!),
            labels = labels
        )
        val response = expensesRepository.editExpense(updated)
        listener?.onCompleted(response)
    }

    fun removeLabelFromExpense(expense: Expense, label: String) {
        val labels = expense.labels.toMutableList()
        if (!labels.remove(label)) return
        val updated = expense.copy(
            timestamp = dateToUTCTimestamp(expense.year!!, expense.month!!, expense.day!!),
            labels = labels
        )
        val response = expensesRepository.editExpense(updated)
        listener?.onCompleted(response)
    }

    fun addExpense(expense: Expense) {
        val response = expensesRepository.addExpense(expense)
        listener?.onCompleted(response)
    }
}
