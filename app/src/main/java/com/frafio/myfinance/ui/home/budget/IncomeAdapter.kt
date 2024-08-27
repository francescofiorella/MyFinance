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
import com.frafio.myfinance.ui.home.budget.IncomeInteractionListener.Companion.ON_LOAD_MORE_REQUEST
import com.frafio.myfinance.ui.home.budget.IncomeInteractionListener.Companion.ON_LONG_CLICK
import com.frafio.myfinance.utils.createTextDrawable

class IncomeAdapter(
    private var incomes: List<Income>,
    private val listener: IncomeInteractionListener
) : RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

    private var currentLimit: Long = DEFAULT_LIMIT

    inner class IncomeViewHolder(
        val binding: LayoutIncomeItemRvBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        IncomeViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_income_item_rv,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val currentIncome = incomes[position]
        holder.binding.income = currentIncome

        if (incomes.size - position < (DEFAULT_LIMIT / 2)) {
            listener.onItemInteraction(ON_LOAD_MORE_REQUEST, currentIncome, position)
        }

        if (currentIncome.category != FirestoreEnums.CATEGORIES.TOTAL.value) {
            holder.binding.categoryIcon.icon =
                createTextDrawable(
                    holder.binding.categoryIcon.context,
                    currentIncome.name!![0].uppercase()
                )
            holder.binding.incomeLayout
                .setOnLongClickListener {
                    listener.onItemInteraction(
                        ON_LONG_CLICK,
                        currentIncome,
                        holder.getAdapterPosition()
                    )
                    true
                }
        } else {
            holder.binding.incomeLayout
                .setOnLongClickListener(null)
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


}