package com.frafio.myfinance.ui.home.list.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.databinding.LayoutTotalItemRvBinding
import com.frafio.myfinance.utils.instantHide
import com.frafio.myfinance.utils.instantShow
import java.text.DateFormatSymbols

class TotalAdapter(
    private var totals: List<Purchase>,
    private val purchaseMap: HashMap<Int, MutableList<Purchase>>,
    private val listener: TotalInteractionListener
) : RecyclerView.Adapter<TotalAdapter.TotalViewHolder>() {

    private var currentMonth: Int? = null

    inner class TotalViewHolder(
        val recyclerViewTotalItemBinding: LayoutTotalItemRvBinding
    ) : RecyclerView.ViewHolder(recyclerViewTotalItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TotalViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_total_item_rv,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: TotalViewHolder, position: Int) {
        val currentTotal = totals[position]
        holder.recyclerViewTotalItemBinding.total = currentTotal

        val purchaseList = purchaseMap[position]!!
        if (purchaseList.size == 0) {
            purchaseList.add(Purchase(name = "Nessun acquisto", type = 0))
        }

        holder.recyclerViewTotalItemBinding.purchasesRV.also {
            it.setHasFixedSize(true)
            it.adapter = PurchaseAdapter(purchaseList)
        }

        if (position != 0) {
            currentMonth = currentTotal.month
            if (currentMonth != totals[position - 1].month) {
                holder.recyclerViewTotalItemBinding.monthLayout.instantShow()
                holder.recyclerViewTotalItemBinding.monthNameTV.text =
                    "${DateFormatSymbols().months[currentMonth!! - 1]} ${currentTotal.year}"
            } else {
                holder.recyclerViewTotalItemBinding.monthLayout.instantHide()
            }
        } else {
            currentMonth = currentTotal.month
            holder.recyclerViewTotalItemBinding.monthNameTV.text =
                "${DateFormatSymbols().months[currentMonth!! - 1]} ${currentTotal.year}"
            holder.recyclerViewTotalItemBinding.monthLayout.instantShow()
        }

/*if (currentPurchase.type == DbPurchases.TYPES.SHOPPING.value) {
    holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout.setOnClickListener {
        listener.onItemInteraction(ON_CLICK, currentPurchase, position)
    }
} else {
    holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout
        .setOnClickListener(null)
}

if (!(currentPurchase.type == DbPurchases.TYPES.TOTAL.value && currentPurchase.price != 0.0)) {
    holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout
        .setOnLongClickListener {
            listener.onItemInteraction(ON_LONG_CLICK, currentPurchase, position)
            true
        }
} else {
    holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout
        .setOnLongClickListener(null)
}


if (currentPurchase.type == DbPurchases.TYPES.TOTAL.value) {
    holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemNomeTextView
        .setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
} else {
    holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemNomeTextView
        .setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
}*/
    }

    override fun getItemCount(): Int {
        return totals.size
    }

    fun updateData(newTotalList: List<Purchase>) {
        val diffUtil = PurchaseDiffUtil(totals, newTotalList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        totals = newTotalList
        diffResult.dispatchUpdatesTo(this)
    }
}