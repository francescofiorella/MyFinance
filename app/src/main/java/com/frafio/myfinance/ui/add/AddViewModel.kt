package com.frafio.myfinance.ui.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.data.repository.IncomeRepository
import com.frafio.myfinance.data.repository.LoadingRepository
import com.frafio.myfinance.ui.navigation.RootKey
import com.frafio.myfinance.utils.dateToExtendedString
import com.frafio.myfinance.utils.dateToUTCTimestamp
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class AddUiEvent {
    data class Success(
        val result: FinanceResult,
        val id: String,
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
    @Assisted private val initialNavKey: RootKey.AddEditTransaction,
    @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle
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

    // TODO add labels in AddActivity
    var labels by mutableStateOf(initialNavKey.transaction?.labels ?: emptyList())

    val dateString: String?
        get() = dateToExtendedString(day, month, year)

    var year by mutableIntStateOf(initialNavKey.transaction?.year ?: LocalDate.now().year)
    var month by mutableIntStateOf(initialNavKey.transaction?.month ?: LocalDate.now().monthValue)
    var day by mutableIntStateOf(initialNavKey.transaction?.day ?: LocalDate.now().dayOfMonth)

    var expenseId by mutableStateOf(initialNavKey.transaction?.id ?: "")
    var expensePosition by mutableIntStateOf(initialNavKey.position ?: 0)

    var nameError by mutableStateOf<String?>(null)
    var priceError by mutableStateOf<String?>(null)
    var categoryError by mutableStateOf<String?>(null)

    private val _uiEvents = MutableSharedFlow<AddUiEvent>()
    val uiEvents: SharedFlow<AddUiEvent> = _uiEvents.asSharedFlow()

    fun load(navKey: RootKey.AddEditTransaction) {
        this.navKey = navKey
        name = navKey.transaction?.name ?: ""
        priceString = navKey.transaction?.price?.let {
            if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
        } ?: ""
        category = navKey.transaction?.category ?: -1
        labels = navKey.transaction?.labels ?: emptyList()
        year = navKey.transaction?.year ?: LocalDate.now().year
        month = navKey.transaction?.month ?: LocalDate.now().monthValue
        day = navKey.transaction?.day ?: LocalDate.now().dayOfMonth
        expenseId = navKey.transaction?.id ?: ""
        expensePosition = navKey.position ?: 0
    }

    fun reset() {
        name = ""
        priceString = ""
        category = -1
        labels = emptyList()
        year = LocalDate.now().year
        month = LocalDate.now().monthValue
        day = LocalDate.now().dayOfMonth
        expenseId = ""
        expensePosition = 0
        navKey = RootKey.AddEditTransaction(REQUEST_ADD_CODE, REQUEST_EXPENSE_CODE)
        nameError = null
        priceError = null
        categoryError = null
    }

    fun onAddButtonClick() {
        viewModelScope.launch {
            try {
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
                } else if (category == -1) {
                    categoryError = FinanceCode.EMPTY_CATEGORY.message
                    hasError = true
                }

                if (hasError) {
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
                                    _uiEvents.emit(AddUiEvent.Success(it, expense.id, true, day, month, year))
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
                                    _uiEvents.emit(AddUiEvent.Success(it, income.id, false, day, month, year))
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
                                    _uiEvents.emit(AddUiEvent.Success(it, expense.id, true, day, month, year))
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
                                    _uiEvents.emit(AddUiEvent.Success(it, income.id, false, day, month, year))
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
