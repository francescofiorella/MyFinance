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
import com.frafio.myfinance.databinding.LayoutTotalItemRvBinding
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_BUTTON_CLICK
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_LOAD_MORE_REQUEST
import com.frafio.myfinance.ui.home.expenses.ExpenseInteractionListener.Companion.ON_LONG_CLICK
import java.time.LocalDate

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val listener: ExpenseInteractionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var currentLimit: Long = DEFAULT_LIMIT

    inner class ExpenseViewHolder(
        val binding: LayoutExpenseItemRvBinding
    ) : RecyclerView.ViewHolder(binding.root)

    inner class TotalViewHolder(
        val binding: LayoutTotalItemRvBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (expenses[position].category == FirestoreEnums.CATEGORIES.TOTAL.value) {
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
            ExpenseViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.layout_expense_item_rv,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentExpense = expenses[position]

        if (expenses.size - position < (DEFAULT_LIMIT / 2)) {
            listener.onItemInteraction(ON_LOAD_MORE_REQUEST, currentExpense, position)
        }

        if (holder.itemViewType == FirestoreEnums.CATEGORIES.TOTAL.value) {
            val tHolder = (holder as TotalViewHolder)
            tHolder.binding.date = currentExpense.getDateString()
            tHolder.binding.amount = currentExpense.price
            return
        }

        val eHolder = holder as ExpenseViewHolder
        eHolder.binding.expense = currentExpense

        if (currentExpense.price == 0.0) {
            eHolder.binding.expenseLayout.setOnLongClickListener(null)
            eHolder.binding.categoryIcon.setOnClickListener(null)
            return
        }
        eHolder.binding.categoryIcon.icon = ContextCompat.getDrawable(
            eHolder.binding.categoryIcon.context,
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
        val types = eHolder.binding.root.context.resources.getStringArray(R.array.categories)
        eHolder.binding.categoryTextView.text =
            types[currentExpense.category ?: FirestoreEnums.CATEGORIES.MISCELLANEOUS.value]
        eHolder.binding.expenseLayout.setOnLongClickListener {
            listener.onItemInteraction(
                ON_LONG_CLICK,
                currentExpense,
                eHolder.getAdapterPosition()
            )
            true
        }

        eHolder.binding.categoryIcon.setOnClickListener {
            listener.onItemInteraction(
                ON_BUTTON_CLICK,
                currentExpense,
                eHolder.getAdapterPosition()
            )
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

    fun getTodayPosition(): Int {
        val today = LocalDate.now()
        val total = expenses.find {
            it.id == "${today.dayOfMonth}_${today.monthValue}_${today.year}"
        }
        return if (total == null) 0 else expenses.indexOf(total)
    }
}