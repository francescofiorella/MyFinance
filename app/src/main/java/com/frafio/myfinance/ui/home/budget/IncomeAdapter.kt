package com.frafio.myfinance.ui.home.budget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.managers.PurchaseManager.Companion.DEFAULT_LIMIT
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.databinding.LayoutPurchaseItemRvBinding
import com.frafio.myfinance.ui.home.budget.IncomeInteractionListener.Companion.ON_LOAD_MORE_REQUEST
import com.frafio.myfinance.ui.home.budget.IncomeInteractionListener.Companion.ON_LONG_CLICK
import com.frafio.myfinance.ui.home.payments.PurchaseDiffUtil
import com.frafio.myfinance.utils.createTextDrawable

class IncomeAdapter(
    private var incomes: List<Purchase>,
    private val listener: IncomeInteractionListener
) : RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

    private var currentLimit: Long = DEFAULT_LIMIT

    inner class IncomeViewHolder(
        val binding: LayoutPurchaseItemRvBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        IncomeViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_purchase_item_rv,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val currentIncome = incomes[position]
        holder.binding.purchase = currentIncome

        if (incomes.size - position < DEFAULT_LIMIT) {
            listener.onItemInteraction(ON_LOAD_MORE_REQUEST, currentIncome, position)
        }

        holder.binding.recViewPurchaseItemPurchaseCategoryIcon.isClickable = false

        if (currentIncome.category != DbPurchases.CATEGORIES.TOTAL.value) {
            holder.binding.recViewPurchaseItemPurchaseCategoryIcon.icon =
                createTextDrawable(
                    holder.binding.recViewPurchaseItemPurchaseCategoryIcon.context,
                    currentIncome.name!![0].uppercase()
                )
            holder.binding.recViewPurchaseItemCategoryTextView.text = currentIncome.getDateString()
            holder.binding.recViewPurchaseItemConstraintLayout
                .setOnLongClickListener {
                    listener.onItemInteraction(
                        ON_LONG_CLICK,
                        currentIncome,
                        holder.getAdapterPosition()
                    )
                    true
                }
        } else {
            holder.binding.recViewPurchaseItemDataTextView.text = currentIncome.year.toString()
            holder.binding.recViewPurchaseItemConstraintLayout
                .setOnLongClickListener(null)
        }
    }

    override fun getItemCount(): Int {
        return incomes.size
    }

    fun updateData(newIncomeList: List<Purchase>) {
        val diffUtil = PurchaseDiffUtil(incomes, newIncomeList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        incomes = newIncomeList
        diffResult.dispatchUpdatesTo(this)
    }

    fun getLimit(increment: Boolean = false): Long {
        if (increment) currentLimit += DEFAULT_LIMIT
        return currentLimit
    }


}