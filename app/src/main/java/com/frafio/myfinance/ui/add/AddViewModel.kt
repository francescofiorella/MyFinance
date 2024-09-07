package com.frafio.myfinance.ui.add

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.widget.DatePickerButton
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.data.repository.IncomeRepository
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.utils.dateToUTCTimestamp
import java.time.LocalDate

class AddViewModel(application: Application) : AndroidViewModel(application) {
    private val expensesRepository = ExpensesRepository(
        (application as MyFinanceApplication).expensesManager
    )
    private val incomeRepository = IncomeRepository(
        (application as MyFinanceApplication).incomesManager
    )

    var listener: AddListener? = null

    var name: String? = null
    var priceString: String? = null
    var category: Int? = null

    var dateString: String? = null

    var year: Int? = LocalDate.now().year
    var month: Int? = LocalDate.now().monthValue
    var day: Int? = LocalDate.now().dayOfMonth

    var expenseId: String? = null
    var expensePosition: Int? = null

    var requestCode: Int? = null
    var expenseCode: Int? = null

    fun updateTime(datePickerBtn: DatePickerButton) {
        year = datePickerBtn.year
        month = datePickerBtn.month
        day = datePickerBtn.day
        dateString = datePickerBtn.dateString
    }

    fun onAddButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        listener?.onAddStart()

        // check info
        if (name.isNullOrEmpty()) {
            listener?.onAddFailure(FinanceResult(FinanceCode.EMPTY_NAME))
            return
        }

        if ((name == FirestoreEnums.NAMES.TOTAL.valueEn || name == FirestoreEnums.NAMES.TOTAL.valueIt)) {
            listener?.onAddFailure(FinanceResult(FinanceCode.WRONG_NAME_TOTAL))
            return
        }

        if (priceString.isNullOrEmpty()) {
            listener?.onAddFailure(FinanceResult(FinanceCode.EMPTY_AMOUNT))
            return
        }
        val price = priceString!!.toDouble()
        if (price == 0.0) {
            listener?.onAddFailure(FinanceResult(FinanceCode.WRONG_AMOUNT))
            return
        }

        if (expenseCode == AddActivity.REQUEST_INCOME_CODE) {
            category = FirestoreEnums.CATEGORIES.INCOME.value
        } else if (category == -1) {
            listener?.onAddFailure(FinanceResult(FinanceCode.EMPTY_CATEGORY))
            return
        }

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
                        expenseId!!
                    )
                    incomeRepository.editIncome(income)
                }
                listener?.onAddSuccess(response)
            }
        }
    }
}