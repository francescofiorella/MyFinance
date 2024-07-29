package com.frafio.myfinance.ui.home.budget

import androidx.recyclerview.widget.DiffUtil
import com.frafio.myfinance.data.models.Income

class IncomeDiffUtil(
    private val oldIncomeList: List<Income>,
    private val newIncomeList: List<Income>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldIncomeList.size
    }

    override fun getNewListSize(): Int {
        return newIncomeList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldIncomeList[oldItemPosition].id == newIncomeList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldIncomeList[oldItemPosition].name != newIncomeList[newItemPosition].name -> false

            oldIncomeList[oldItemPosition].price != newIncomeList[newItemPosition].price -> false

            oldIncomeList[oldItemPosition].year != newIncomeList[newItemPosition].year -> false

            oldIncomeList[oldItemPosition].month != newIncomeList[newItemPosition].month -> false

            oldIncomeList[oldItemPosition].day != newIncomeList[newItemPosition].day -> false

            oldIncomeList[oldItemPosition].timestamp != newIncomeList[newItemPosition].timestamp -> false

            oldIncomeList[oldItemPosition].category != newIncomeList[newItemPosition].category -> false

            else -> true
        }
    }
}