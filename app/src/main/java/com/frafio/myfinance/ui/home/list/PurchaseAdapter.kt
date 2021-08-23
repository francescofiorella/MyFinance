package com.frafio.myfinance.ui.home.list

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.databinding.RecyclerViewPurchaseItemBinding
import com.frafio.myfinance.ui.home.list.PurchaseInteractionListener.Companion.ON_CLICK
import com.frafio.myfinance.ui.home.list.PurchaseInteractionListener.Companion.ON_LONG_CLICK

class PurchaseAdapter(
    private val purchases: List<Purchase>,
    private val listener: PurchaseInteractionListener
) : RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder>() {

    inner class PurchaseViewHolder(
        val recyclerViewPurchaseItemBinding: RecyclerViewPurchaseItemBinding
    ) : RecyclerView.ViewHolder(recyclerViewPurchaseItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PurchaseViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.recycler_view_purchase_item,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        val currentPurchase = purchases[position]
        holder.recyclerViewPurchaseItemBinding.purchase = currentPurchase

        if (currentPurchase.type == DbPurchases.TYPES.SHOPPING.value) {
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout.setOnClickListener {
                listener.onItemInteraction(ON_CLICK, currentPurchase, position)
            }
        } else {
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout
                .setOnClickListener(null)
        }

        if (!(currentPurchase.type == 0 && currentPurchase.price != 0.0)) {
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout
                .setOnLongClickListener {
                    listener.onItemInteraction(ON_LONG_CLICK, currentPurchase, position)
                    true
                }
        } else {
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout
                .setOnLongClickListener(null)
        }


        if (currentPurchase.type == 0) {
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemNomeTextView
                .setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        } else {
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemNomeTextView
                .setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        }
    }

    override fun getItemCount(): Int {
        return purchases.size
    }
}