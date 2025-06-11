package com.frafio.myfinance.ui.home.budget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.manager.IncomesManager.Companion.DEFAULT_LIMIT
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.databinding.LayoutIncomeItemRvBinding
import com.frafio.myfinance.databinding.LayoutTotalItemRvBinding
import com.frafio.myfinance.ui.home.budget.IncomeInteractionListener.Companion.ON_LOAD_MORE_REQUEST
import com.frafio.myfinance.ui.home.budget.IncomeInteractionListener.Companion.ON_LONG_CLICK
import com.frafio.myfinance.utils.createTextDrawable

class IncomeAdapter(
    private var incomes: List<Income>,
    private val listener: IncomeInteractionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var currentLimit: Long = DEFAULT_LIMIT

    inner class IncomeViewHolder(
        val binding: LayoutIncomeItemRvBinding
    ) : RecyclerView.ViewHolder(binding.root)

    inner class TotalViewHolder(
        val binding: LayoutTotalItemRvBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (incomes[position].category == FirestoreEnums.CATEGORIES.TOTAL.value) {
            FirestoreEnums.CATEGORIES.TOTAL.value
        } else {
            super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == FirestoreEnums.CATEGORIES.TOTAL.value) {
            TotalViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.layout_total_item_rv,
                    parent,
                    false
                )
            )
        } else {
            IncomeViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.layout_income_item_rv,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentIncome = incomes[position]

        if (incomes.size - position < (DEFAULT_LIMIT / 2)) {
            listener.onItemInteraction(ON_LOAD_MORE_REQUEST, currentIncome, position)
        }

        if (holder.itemViewType == FirestoreEnums.CATEGORIES.TOTAL.value) {
            val tHolder = (holder as TotalViewHolder)
            tHolder.binding.date = currentIncome.year.toString()
            tHolder.binding.amount = currentIncome.price
            return
        }

        val iHolder = holder as IncomeViewHolder
        iHolder.binding.income = currentIncome

        if (currentIncome.price == 0.0) {
            iHolder.binding.incomeLayout.setOnLongClickListener(null)
            return
        }
        iHolder.binding.categoryIcon.icon =
            iHolder.binding.categoryIcon.context.createTextDrawable(
                currentIncome.name!![0].uppercase()
            )
        iHolder.binding.incomeLayout.setOnLongClickListener {
            listener.onItemInteraction(
                ON_LONG_CLICK,
                currentIncome,
                iHolder.getAdapterPosition()
            )
            true
        }
    }

    override fun getItemCount(): Int {
        return incomes.size
    }

    fun updateData(newIncomeList: List<Income>) {
        val diffUtil = IncomeDiffUtil(incomes, newIncomeList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        incomes = newIncomeList
        diffResult.dispatchUpdatesTo(this)
    }

    fun getLimit(increment: Boolean = false): Long {
        if (increment) currentLimit += (DEFAULT_LIMIT / 2)
        return currentLimit
    }

    fun getItemPositionWithId(id: String): Int {
        val i = incomes.find { it.id == id }
        if (i != null) {
            return incomes.indexOf(i)
        }
        return 0
    }
}