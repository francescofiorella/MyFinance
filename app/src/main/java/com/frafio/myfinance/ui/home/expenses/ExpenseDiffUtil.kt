package com.frafio.myfinance.ui.home.expenses

import androidx.recyclerview.widget.DiffUtil
import com.frafio.myfinance.data.model.Expense

class ExpenseDiffUtil(
    private val oldExpenseList: List<Expense>,
    private val newExpenseList: List<Expense>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldExpenseList.size
    }

    override fun getNewListSize(): Int {
        return newExpenseList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldExpenseList[oldItemPosition].id == newExpenseList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldExpenseList[oldItemPosition].name != newExpenseList[newItemPosition].name -> false

            oldExpenseList[oldItemPosition].price != newExpenseList[newItemPosition].price -> false

            oldExpenseList[oldItemPosition].year != newExpenseList[newItemPosition].year -> false

            oldExpenseList[oldItemPosition].month != newExpenseList[newItemPosition].month -> false

            oldExpenseList[oldItemPosition].day != newExpenseList[newItemPosition].day -> false

            oldExpenseList[oldItemPosition].timestamp != newExpenseList[newItemPosition].timestamp -> false

            oldExpenseList[oldItemPosition].category != newExpenseList[newItemPosition].category -> false

            else -> true
        }
    }
}