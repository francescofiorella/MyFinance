package com.frafio.myfinance.ui.home.expenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.utils.dateToUTCTimestamp

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {
    private val expensesRepository = ExpensesRepository(
        (application as MyFinanceApplication).expensesManager
    )
    private val expensesLocalRepository = ExpensesLocalRepository()

    var listener: ExpensesListener? = null

    val isExpensesEmpty = MutableLiveData<Boolean?>(null)

    fun getExpensesNumber(): LiveData<Int> {
        return expensesLocalRepository.getCount()
    }

    fun getLocalExpenses(): LiveData<List<Expense>> {
        return expensesLocalRepository.getAll()
    }

    fun deleteExpense(expense: Expense) {
        val response = expensesRepository.deleteExpense(expense)
        listener?.onDeleteCompleted(response, expense)
    }

    fun updateCategory(expense: Expense, newCategory: Int) {
        val updated = Expense(
            name = expense.name,
            price = expense.price,
            year = expense.year,
            month = expense.month,
            day = expense.day,
            timestamp = dateToUTCTimestamp(expense.year!!, expense.month!!, expense.day!!),
            category = newCategory,
            id = expense.id
        )
        val response = expensesRepository.editExpense(updated)
        listener?.onCompleted(response)
    }

    fun addExpense(expense: Expense) {
        val response = expensesRepository.addExpense(expense)
        listener?.onCompleted(response)
    }

    fun filterExpenses(name: String): LiveData<List<Expense>> {
        return expensesLocalRepository.getStartingWith(name)
    }
}