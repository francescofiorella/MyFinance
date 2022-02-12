package com.frafio.myfinance.ui.home.list.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.databinding.LayoutDailyPurchaseRvBinding

class PurchaseAdapter(
    private var purchases: List<Purchase>
) : RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder>() {

    inner class PurchaseViewHolder(
        val recyclerViewPurchaseItemBinding: LayoutDailyPurchaseRvBinding
    ) : RecyclerView.ViewHolder(recyclerViewPurchaseItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PurchaseViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_daily_purchase_rv,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PurchaseAdapter.PurchaseViewHolder, position: Int) {
        val currentPurchase = purchases[position]
        holder.recyclerViewPurchaseItemBinding.purchase = currentPurchase

        if (currentPurchase.type == 0) {
            holder.recyclerViewPurchaseItemBinding.purchasePriceTV.visibility = View.GONE
        } else {
            holder.recyclerViewPurchaseItemBinding.purchasePriceTV.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return purchases.size
    }

    fun updateData(newPurchaseList: List<Purchase>) {
        val diffUtil = PurchaseDiffUtil(purchases, newPurchaseList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        purchases = newPurchaseList
        diffResult.dispatchUpdatesTo(this)
    }
}