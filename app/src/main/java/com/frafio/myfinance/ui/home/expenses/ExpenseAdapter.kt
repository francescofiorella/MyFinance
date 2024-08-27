package com.frafio.myfinance.ui.home.expenses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.manager.ExpensesManager.Companion.DEFAULT_LIMIT
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.databinding.LayoutExpenseItemRvBinding
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_BUTTON_CLICK
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_LOAD_MORE_REQUEST
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_LONG_CLICK

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val listener: ExpenseInteractionListener
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private var currentLimit: Long = DEFAULT_LIMIT

    inner class ExpenseViewHolder(
        val binding: LayoutExpenseItemRvBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ExpenseViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_expense_item_rv,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val currentExpense = expenses[position]
        holder.binding.expense = currentExpense

        if (expenses.size - position < (DEFAULT_LIMIT / 2)) {
            listener.onItemInteraction(ON_LOAD_MORE_REQUEST, currentExpense, position)
        }

        if (currentExpense.category != FirestoreEnums.CATEGORIES.TOTAL.value) {
            holder.binding.categoryIcon.icon = ContextCompat.getDrawable(
                holder.binding.categoryIcon.context,
                when (currentExpense.category) {
                    FirestoreEnums.CATEGORIES.HOUSING.value -> R.drawable.ic_baseline_home
                    FirestoreEnums.CATEGORIES.GROCERIES.value -> R.drawable.ic_shopping_cart
                    FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> R.drawable.ic_self_care
                    FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy
                    FirestoreEnums.CATEGORIES.EDUCATION.value -> R.drawable.ic_school
                    FirestoreEnums.CATEGORIES.DINING.value -> R.drawable.ic_restaurant
                    FirestoreEnums.CATEGORIES.HEALTH.value -> R.drawable.ic_vaccines
                    FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> R.drawable.ic_directions_transit
                    FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> R.drawable.ic_tag
                    else -> R.drawable.ic_tag
                }
            )
            val types =
                holder.binding.categoryTextView.context.resources.getStringArray(R.array.categories)
            holder.binding.categoryTextView.text =
                types[currentExpense.category ?: FirestoreEnums.CATEGORIES.MISCELLANEOUS.value]
            holder.binding.expenseLayout.setOnLongClickListener {
                listener.onItemInteraction(
                    ON_LONG_CLICK,
                    currentExpense,
                    holder.getAdapterPosition()
                )
                true
            }

            holder.binding.categoryIcon.setOnClickListener {
                listener.onItemInteraction(
                    ON_BUTTON_CLICK,
                    currentExpense,
                    holder.getAdapterPosition()
                )
            }
        } else {
            holder.binding.expenseLayout.setOnLongClickListener(null)
            holder.binding.categoryIcon.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int {
        return expenses.size
    }

    fun updateData(newExpenseList: List<Expense>) {
        val diffUtil = ExpenseDiffUtil(expenses, newExpenseList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        expenses = newExpenseList
        diffResult.dispatchUpdatesTo(this)
    }

    fun getLimit(increment: Boolean = false): Long {
        if (increment) currentLimit += (DEFAULT_LIMIT / 2)
        return currentLimit
    }
}