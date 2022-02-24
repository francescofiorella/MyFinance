package com.frafio.myfinance.ui.home.list.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.databinding.LayoutTotalDialogBodyItemBinding

class DialogPurchaseAdapter(
    private var purchases: List<Purchase>
) : RecyclerView.Adapter<DialogPurchaseAdapter.PurchaseViewHolder>() {

    inner class PurchaseViewHolder(
        val recyclerViewDialogBodyItemBinding: LayoutTotalDialogBodyItemBinding
    ) : RecyclerView.ViewHolder(recyclerViewDialogBodyItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PurchaseViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_total_dialog_body_item,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: DialogPurchaseAdapter.PurchaseViewHolder, position: Int) {
        val currentPurchase = purchases[position]
        holder.recyclerViewDialogBodyItemBinding.purchase = currentPurchase
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