package com.frafio.myfinance.features.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.FinanceResult
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.repository.ExpensesRepository
import com.frafio.myfinance.core.data.repository.IncomeRepository
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.core.utils.dateToExtendedString
import com.frafio.myfinance.core.utils.dateToUTCTimestamp
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class AddUiEvent {
    data class Success(
        val result: FinanceResult,
        val isExpense: Boolean,
        val day: Int,
        val month: Int,
        val year: Int
    ) : AddUiEvent()
    data class Error(val result: FinanceResult) : AddUiEvent()
}

@HiltViewModel(assistedFactory = AddViewModel.Factory::class)
class AddViewModel @AssistedInject constructor(
    private val expensesRepository: ExpensesRepository,
    private val incomeRepository: IncomeRepository,
    private val loadingRepository: LoadingRepository,
    userPreferencesRepository: UserPreferencesRepository,
    @Assisted private val initialNavKey: RootKey.AddEditTransaction
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: RootKey.AddEditTransaction): AddViewModel
    }

    companion object {
        const val REQUEST_ADD_CODE: Int = 1
        const val REQUEST_EDIT_CODE: Int = 2
        const val REQUEST_EXPENSE_CODE: Int = 10
        const val REQUEST_INCOME_CODE: Int = 11
    }

    var navKey by mutableStateOf(initialNavKey)

    var name by mutableStateOf(initialNavKey.transaction?.name ?: "")
    var priceString by mutableStateOf(
        initialNavKey.transaction?.price?.let {
            if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
        } ?: ""
    )
    var category by mutableIntStateOf(initialNavKey.transaction?.category ?: -1)

    var labels by mutableStateOf(initialNavKey.transaction?.labels ?: emptyList())

    val allLabels: StateFlow<List<String>> = userPreferencesRepository.userPreferencesFlow
        .map { it.labels }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onLabelCheckedChanged(label: String, checked: Boolean) {
        if (checked) {
            if (!labels.contains(label)) labels = labels + label
        } else {
            labels = labels - label
        }
    }

    fun addLabel(label: String) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val currentAllLabels = allLabels.value
                if (currentAllLabels.contains(label)) return@launch
                expensesRepository.setLabels(currentAllLabels + label)
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun deleteLabel(label: String) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val currentAllLabels = allLabels.value.toMutableList()
                if (currentAllLabels.remove(label)) {
                    expensesRepository.setLabels(currentAllLabels, FinanceCode.LABEL_DELETE_SUCCESS)
                    // Also remove from selected labels
                    labels = labels - label
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun editLabel(oldName: String, newName: String) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val currentAllLabels = allLabels.value.toMutableList()
                val index = currentAllLabels.indexOf(oldName)
                if (index != -1) {
                    currentAllLabels[index] = newName
                    expensesRepository.setLabels(currentAllLabels, FinanceCode.LABEL_UPDATE_SUCCESS)

                    // Update current selected labels if it contained the old name
                    if (labels.contains(oldName)) {
                        labels = labels.map { if (it == oldName) newName else it }
                    }
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    val dateString: String?
        get() = dateToExtendedString(day, month, year)

    var year by mutableIntStateOf(initialNavKey.transaction?.year ?: LocalDate.now().year)
    var month by mutableIntStateOf(initialNavKey.transaction?.month ?: LocalDate.now().monthValue)
    var day by mutableIntStateOf(initialNavKey.transaction?.day ?: LocalDate.now().dayOfMonth)

    var expenseId by mutableStateOf(initialNavKey.transaction?.id ?: "")

    var nameError by mutableStateOf<String?>(null)
    var priceError by mutableStateOf<String?>(null)
    var categoryError by mutableStateOf<String?>(null)

    fun resetErrors() {
        nameError = null
        priceError = null
        categoryError = null
    }

    private val _uiEvents = MutableSharedFlow<AddUiEvent>()
    val uiEvents: SharedFlow<AddUiEvent> = _uiEvents.asSharedFlow()

    private val _isAdding: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isAdding: StateFlow<Boolean> = _isAdding.asStateFlow()

    fun updateAddingState(isAdding: Boolean) {
        _isAdding.value = isAdding
    }

    fun onAddButtonClick() {
        viewModelScope.launch {
            try {
                updateAddingState(true)
                loadingRepository.startLoading()

                // check info
                var hasError = false
                nameError = null
                priceError = null
                categoryError = null

                if (name.isEmpty()) {
                    nameError = FinanceCode.EMPTY_NAME.message
                    hasError = true
                } else if (name == FirestoreEnums.NAMES.TOTAL.valueEn || name == FirestoreEnums.NAMES.TOTAL.valueIt) {
                    nameError = FinanceCode.WRONG_NAME_TOTAL.message
                    hasError = true
                }

                if (priceString.isEmpty()) {
                    priceError = FinanceCode.EMPTY_AMOUNT.message
                    hasError = true
                } else if (priceString.toDoubleOrNull() == null || priceString.toDouble() == 0.0) {
                    priceError = FinanceCode.WRONG_AMOUNT.message
                    hasError = true
                }

                if (navKey.expenseCode == REQUEST_INCOME_CODE) {
                    category = FirestoreEnums.CATEGORIES.INCOME.value
                    labels = emptyList()
                } else if (category == -1) {
                    categoryError = FinanceCode.EMPTY_CATEGORY.message
                    hasError = true
                }

                if (hasError) {
                    updateAddingState(false)
                    return@launch
                }

                val price = priceString.toDouble()

                when (navKey.requestCode) {
                    REQUEST_ADD_CODE -> {
                        if (navKey.expenseCode == REQUEST_EXPENSE_CODE) {
                            val expense = Expense(
                                name = name,
                                price = price,
                                year = year,
                                month = month,
                                day = day,
                                timestamp = dateToUTCTimestamp(year, month, day),
                                category = category,
                                labels = labels
                            )
                            expensesRepository.addExpense(expense).also {
                                if (it.code == FinanceCode.EXPENSE_ADD_SUCCESS.code) {
                                    _uiEvents.emit(AddUiEvent.Success(it, true, day, month, year))
                                } else {
                                    _uiEvents.emit(AddUiEvent.Error(it))
                                }
                            }
                        } else {
                            val income = Income(
                                name = name,
                                price = price,
                                year = year,
                                month = month,
                                day = day,
                                timestamp = dateToUTCTimestamp(year, month, day),
                                category = category,
                                labels = labels
                            )
                            incomeRepository.addIncome(income).also {
                                if (it.code == FinanceCode.INCOME_ADD_SUCCESS.code) {
                                    _uiEvents.emit(AddUiEvent.Success(it, false, day, month, year))
                                } else {
                                    _uiEvents.emit(AddUiEvent.Error(it))
                                }
                            }
                        }
                    }

                    REQUEST_EDIT_CODE -> {
                        if (navKey.expenseCode == REQUEST_EXPENSE_CODE) {
                            val expense = Expense(
                                name = name,
                                price = price,
                                year = year,
                                month = month,
                                day = day,
                                timestamp = dateToUTCTimestamp(year, month, day),
                                category = category,
                                labels = labels,
                                id = expenseId
                            )
                            expensesRepository.editExpense(expense).also {
                                if (it.code == FinanceCode.EXPENSE_EDIT_SUCCESS.code) {
                                    _uiEvents.emit(AddUiEvent.Success(it, true, day, month, year))
                                } else {
                                    _uiEvents.emit(AddUiEvent.Error(it))
                                }
                            }
                        } else {
                            val income = Income(
                                name = name,
                                price = price,
                                year = year,
                                month = month,
                                day = day,
                                timestamp = dateToUTCTimestamp(year, month, day),
                                category = category,
                                labels = labels,
                                id = expenseId
                            )
                            incomeRepository.editIncome(income).also {
                                if (it.code == FinanceCode.INCOME_EDIT_SUCCESS.code) {
                                    _uiEvents.emit(AddUiEvent.Success(it, false, day, month, year))
                                } else {
                                    _uiEvents.emit(AddUiEvent.Error(it))
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
