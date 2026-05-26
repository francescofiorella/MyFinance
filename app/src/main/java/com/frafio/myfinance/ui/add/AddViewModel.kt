package com.frafio.myfinance.ui.add

import android.view.View
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.data.repository.IncomeRepository
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.utils.dateToExtendedString
import com.frafio.myfinance.utils.dateToUTCTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private val expensesRepository: ExpensesRepository,
    private val incomeRepository: IncomeRepository
) : ViewModel() {

    var listener: AddListener? = null

    var name: String? = null
    var priceString: String? = null
    var category: Int? = null

    // TODO add labels in AddActivity
    var labels: List<String>? = null

    val dateString: String?
        get() = dateToExtendedString(day, month, year)

    var year: Int? = LocalDate.now().year
    var month: Int? = LocalDate.now().monthValue
    var day: Int? = LocalDate.now().dayOfMonth

    var expenseId: String? = null
    var expensePosition: Int? = null

    var requestCode: Int? = null
    var expenseCode: Int? = null

    fun onAddButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        listener?.onAddStart()

        // check info
        var hasError = false
        if (name.isNullOrEmpty()) {
            listener?.onAddFailure(FinanceResult(FinanceCode.EMPTY_NAME))
            hasError = true
        } else if (name == FirestoreEnums.NAMES.TOTAL.valueEn || name == FirestoreEnums.NAMES.TOTAL.valueIt) {
            listener?.onAddFailure(FinanceResult(FinanceCode.WRONG_NAME_TOTAL))
            hasError = true
        }

        if (priceString.isNullOrEmpty()) {
            listener?.onAddFailure(FinanceResult(FinanceCode.EMPTY_AMOUNT))
            hasError = true
        } else if (priceString!!.toDouble() == 0.0) {
            listener?.onAddFailure(FinanceResult(FinanceCode.WRONG_AMOUNT))
            hasError = true
        }

        if (expenseCode == AddActivity.REQUEST_INCOME_CODE) {
            category = FirestoreEnums.CATEGORIES.INCOME.value
        } else if (category == -1) {
            listener?.onAddFailure(FinanceResult(FinanceCode.EMPTY_CATEGORY))
            hasError = true
        }

        if (hasError) return

        val price = priceString!!.toDouble()

        when (requestCode) {
            AddActivity.REQUEST_ADD_CODE -> {
                val response = if (expenseCode == AddActivity.REQUEST_EXPENSE_CODE) {
                    val expense = Expense(
                        name,
                        price,
                        year,
                        month,
                        day,
                        dateToUTCTimestamp(year!!, month!!, day!!),
                        category
                    )
                    expensesRepository.addExpense(expense)
                } else {
                    val income = Income(
                        name,
                        price,
                        year,
                        month,
                        day,
                        dateToUTCTimestamp(year!!, month!!, day!!),
                        category
                    )
                    incomeRepository.addIncome(income)
                }
                listener?.onAddSuccess(response)
            }

            AddActivity.REQUEST_EDIT_CODE -> {
                val response = if (expenseCode == AddActivity.REQUEST_EXPENSE_CODE) {
                    val expense = Expense(
                        name,
                        price,
                        year,
                        month,
                        day,
                        dateToUTCTimestamp(year!!, month!!, day!!),
                        category,
                        labels ?: emptyList(),
                        expenseId!!
                    )
                    expensesRepository.editExpense(expense)
                } else {
                    val income = Income(
                        name,
                        price,
                        year,
                        month,
                        day,
                        dateToUTCTimestamp(year!!, month!!, day!!),
                        category,
                        labels ?: emptyList(),
                        expenseId!!
                    )
                    incomeRepository.editIncome(income)
                }
                listener?.onAddSuccess(response)
            }
        }
    }
}
