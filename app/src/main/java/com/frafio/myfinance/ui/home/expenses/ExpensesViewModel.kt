package com.frafio.myfinance.ui.home.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.manager.ExpensesManager.Companion.DEFAULT_LIMIT
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.data.repository.LoadingRepository
import com.frafio.myfinance.data.repository.UserPreferencesRepository
import com.frafio.myfinance.utils.addTotalsToExpenses
import com.frafio.myfinance.utils.addTotalsToExpensesWithoutToday
import com.frafio.myfinance.utils.dateToUTCTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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
import javax.inject.Inject

sealed class ExpensesUiEvent {
    data class ShowSnackBar(
        val message: String,
        val actionText: String? = null,
        val actionFun: () -> Unit = {},
        val dismissFun: () -> Unit = {}
    ) : ExpensesUiEvent()
    data class ExpenseDeleted(val expense: Expense) : ExpensesUiEvent()
    object LabelDeleted : ExpensesUiEvent()
}

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val expensesRepository: ExpensesRepository,
    private val expensesLocalRepository: ExpensesLocalRepository,
    userPreferencesRepository: UserPreferencesRepository,
    private val loadingRepository: LoadingRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategories = MutableStateFlow<List<Int>>(emptyList())
    val selectedCategories = _selectedCategories.asStateFlow()

    private val _selectedLabels = MutableStateFlow<List<String>>(emptyList())
    val selectedLabels = _selectedLabels.asStateFlow()

    private val _dateRange = MutableStateFlow<Pair<LocalDate, LocalDate>?>(null)
    val dateRange = _dateRange.asStateFlow()

    private val _limit = MutableStateFlow(DEFAULT_LIMIT)

    private val _scrollToId = MutableSharedFlow<String?>(replay = 1)
    val scrollToId = _scrollToId.asSharedFlow()

    private val _editingExpense = MutableStateFlow<Expense?>(null)
    val editingExpense = _editingExpense.asStateFlow()

    private val _uiEvents = MutableSharedFlow<ExpensesUiEvent>()
    val uiEvents: SharedFlow<ExpensesUiEvent> = _uiEvents.asSharedFlow()

    val labels: StateFlow<List<String>> = userPreferencesRepository.userPreferencesFlow
        .map { it.labels }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isExpensesEmpty: StateFlow<Boolean?> = expensesLocalRepository.getCount()
        .map { it == 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val allFilteredExpenses: Flow<List<Expense>> = combine(
        _searchQuery,
        _selectedCategories,
        _selectedLabels,
        _dateRange
    ) { query, categories, labels, dateRange ->
        FilterParams(query, categories, labels, dateRange)
    }.flatMapLatest { params ->
        val effectiveCategories = params.categories.ifEmpty {
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

        val flow = if (params.dateRange == null) {
            expensesLocalRepository.getWithFilter(params.query, effectiveCategories)
        } else {
            expensesLocalRepository.getWithFilterDate(
                params.query,
                effectiveCategories,
                dateToUTCTimestamp(params.dateRange.first),
                dateToUTCTimestamp(params.dateRange.second.plusDays(1))
            )
        }

        if (params.labels.isEmpty()) {
            flow
        } else {
            flow.map { list ->
                list.filter { expense ->
                    expense.labels.any { it in params.labels }
                }
            }
        }
    }

    val expenses: StateFlow<List<Expense>> = allFilteredExpenses
        .combine(_limit) { list, limit ->
            list.take(limit.toInt())
        }.map { limitedList ->
            if (_searchQuery.value.isEmpty() && _selectedCategories.value.isEmpty() &&
                _selectedLabels.value.isEmpty() && _dateRange.value == null
            ) {
                addTotalsToExpenses(limitedList)
            } else {
                addTotalsToExpensesWithoutToday(limitedList)
            }
        }.flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFilteredExpenses: StateFlow<Double> = allFilteredExpenses
        .map { list -> list.sumOf { it.price ?: 0.0 } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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

    fun onLabelFilterChanged(label: String, active: Boolean) {
        val current = _selectedLabels.value.toMutableList()
        if (active) {
            if (!current.contains(label)) {
                current.add(label)
            }
        } else {
            current.remove(label)
        }
        _selectedLabels.value = current
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

    fun setEditingExpense(expense: Expense?) {
        _editingExpense.value = expense
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val response = expensesRepository.deleteExpense(expense)
                if (response.code == FinanceCode.EXPENSE_DELETE_SUCCESS.code) {
                    _uiEvents.emit(ExpensesUiEvent.ExpenseDeleted(expense))
                } else {
                    _uiEvents.emit(ExpensesUiEvent.ShowSnackBar(response.message))
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun updateCategory(expense: Expense, newCategory: Int) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val updated = expense.copy(
                    timestamp = dateToUTCTimestamp(expense.year!!, expense.month!!, expense.day!!),
                    category = newCategory
                )
                val response = expensesRepository.editExpense(updated)
                if (response.code == FinanceCode.EXPENSE_EDIT_FAILURE.code) {
                    _uiEvents.emit(ExpensesUiEvent.ShowSnackBar(response.message))
                } else {
                    if (_editingExpense.value?.id == expense.id) {
                        _editingExpense.value = updated
                    }
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun addLabel(label: String) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val currentLabels = labels.value
                if (currentLabels.contains(label)) return@launch
                val response = expensesRepository.setLabels(currentLabels + label)
                if (response.code == FinanceCode.LABELS_UPDATE_FAILURE.code) {
                    _uiEvents.emit(ExpensesUiEvent.ShowSnackBar(response.message))
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun addLabelToExpense(expense: Expense, label: String) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                if (expense.labels.contains(label)) return@launch
                val labels = expense.labels.toMutableList()
                labels.add(label)
                val updated = expense.copy(
                    timestamp = dateToUTCTimestamp(expense.year!!, expense.month!!, expense.day!!),
                    labels = labels
                )
                val response = expensesRepository.editExpense(updated)
                if (response.code == FinanceCode.EXPENSE_EDIT_FAILURE.code) {
                    _uiEvents.emit(ExpensesUiEvent.ShowSnackBar(response.message))
                } else {
                    if (_editingExpense.value?.id == expense.id) {
                        _editingExpense.value = updated
                    }
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun removeLabelFromExpense(expense: Expense, label: String) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val labels = expense.labels.toMutableList()
                if (!labels.remove(label)) return@launch
                val updated = expense.copy(
                    timestamp = dateToUTCTimestamp(expense.year!!, expense.month!!, expense.day!!),
                    labels = labels
                )
                val response = expensesRepository.editExpense(updated)
                if (response.code == FinanceCode.EXPENSE_EDIT_FAILURE.code) {
                    _uiEvents.emit(ExpensesUiEvent.ShowSnackBar(response.message))
                } else {
                    if (_editingExpense.value?.id == expense.id) {
                        _editingExpense.value = updated
                    }
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun addExpense(expense: Expense, notify: Boolean = true) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val response = expensesRepository.addExpense(expense)
                if (notify) {
                    _uiEvents.emit(ExpensesUiEvent.ShowSnackBar(response.message))
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    private var lastDeletedLabel: String? = null
    private var expensesWithDeletedLabel: List<Expense> = emptyList()

    fun deleteLabel(label: String) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val currentLabels = labels.value.toMutableList()
                if (currentLabels.remove(label)) {
                    lastDeletedLabel = label
                    val response = expensesRepository.setLabels(
                        currentLabels,
                        FinanceCode.LABEL_DELETE_SUCCESS
                    )
                    if (response.code == FinanceCode.LABELS_UPDATE_FAILURE.code) {
                        _uiEvents.emit(ExpensesUiEvent.ShowSnackBar(response.message))
                        return@launch
                    }
                    _uiEvents.emit(ExpensesUiEvent.LabelDeleted)

                    viewModelScope.launch(Dispatchers.IO) {
                        val allExpenses = expensesLocalRepository.getAllSync()
                        expensesWithDeletedLabel = allExpenses.filter { it.labels.contains(label) }

                        expensesWithDeletedLabel.forEach { expense ->
                            val updatedLabels = expense.labels.toMutableList()
                            updatedLabels.remove(label)
                            val updatedExpense = expense.copy(
                                timestamp = dateToUTCTimestamp(
                                    expense.year!!,
                                    expense.month!!,
                                    expense.day!!
                                ),
                                labels = updatedLabels
                            )
                            expensesRepository.editExpense(updatedExpense)
                        }
                    }
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun undoDeleteLabel() {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val label = lastDeletedLabel ?: return@launch
                val currentLabels = labels.value.toMutableList()
                if (!currentLabels.contains(label)) {
                    currentLabels.add(label)
                    val response = expensesRepository.setLabels(currentLabels)
                    if (response.code == FinanceCode.LABELS_UPDATE_FAILURE.code) {
                        _uiEvents.emit(ExpensesUiEvent.ShowSnackBar(response.message))
                        return@launch
                    }

                    viewModelScope.launch(Dispatchers.IO) {
                        expensesWithDeletedLabel.forEach { expense ->
                            expensesRepository.editExpense(expense)
                        }
                        resetLastDeletedLabel()
                    }
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun resetLastDeletedLabel() {
        lastDeletedLabel = null
        expensesWithDeletedLabel = emptyList()
    }

    fun editLabel(oldName: String, newName: String) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val currentLabels = labels.value.toMutableList()
                val index = currentLabels.indexOf(oldName)
                if (index != -1) {
                    currentLabels[index] = newName
                    val response = expensesRepository.setLabels(
                        currentLabels,
                        FinanceCode.LABEL_UPDATE_SUCCESS
                    )
                    if (response.code == FinanceCode.LABELS_UPDATE_FAILURE.code) {
                        _uiEvents.emit(ExpensesUiEvent.ShowSnackBar(response.message))
                        return@launch
                    }

                    viewModelScope.launch(Dispatchers.IO) {
                        val allExpenses = expensesLocalRepository.getAllSync()
                        val expensesToUpdate = allExpenses.filter { it.labels.contains(oldName) }

                        expensesToUpdate.forEach { expense ->
                            val updatedLabels = expense.labels.toMutableList()
                            val labelIndex = updatedLabels.indexOf(oldName)
                            if (labelIndex != -1) {
                                updatedLabels[labelIndex] = newName
                                val updatedExpense = expense.copy(
                                    timestamp = dateToUTCTimestamp(
                                        expense.year!!,
                                        expense.month!!,
                                        expense.day!!
                                    ),
                                    labels = updatedLabels
                                )
                                expensesRepository.editExpense(updatedExpense)
                                if (_editingExpense.value?.id == expense.id) {
                                    _editingExpense.value = updatedExpense
                                }
                            }
                        }
                    }
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }
}

private data class FilterParams(
    val query: String,
    val categories: List<Int>,
    val labels: List<String>,
    val dateRange: Pair<LocalDate, LocalDate>?
)
