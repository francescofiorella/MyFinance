package com.frafio.myfinance.ui.home.payments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.databinding.LayoutPurchaseItemRvBinding
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_BUTTON_CLICK
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_LONG_CLICK
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_PROGRESS_INDICATOR_SHOWN

class PurchaseAdapter(
    private var purchases: List<Purchase>,
    private val listener: PurchaseInteractionListener
) : RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder>() {

    private var currentLimit: Long = 30

    inner class PurchaseViewHolder(
        val recyclerViewPurchaseItemBinding: LayoutPurchaseItemRvBinding
    ) : RecyclerView.ViewHolder(recyclerViewPurchaseItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PurchaseViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_purchase_item_rv,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        val currentPurchase = purchases[position]
        holder.recyclerViewPurchaseItemBinding.purchase = currentPurchase

        if (currentPurchase.id == "progressIndicator") {
            listener.onItemInteraction(ON_PROGRESS_INDICATOR_SHOWN, currentPurchase, position)
            holder.recyclerViewPurchaseItemBinding.loadProgressIndicator.visibility = View.VISIBLE
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemDataLayout.visibility =
                View.GONE
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout.visibility =
                View.GONE
        } else {
            holder.recyclerViewPurchaseItemBinding.loadProgressIndicator.visibility = View.GONE
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout.visibility =
                View.VISIBLE
        }

        if (currentPurchase.type != DbPurchases.TYPES.TOTAL.value) {
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemPurchaseTypeIcon.icon =
                ContextCompat.getDrawable(
                    holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemPurchaseTypeIcon.context,
                    when (purchases[position].type) {
                        DbPurchases.TYPES.HOUSING.value -> R.drawable.ic_baseline_home
                        DbPurchases.TYPES.GROCERIES.value -> R.drawable.ic_shopping_cart
                        DbPurchases.TYPES.PERSONAL_CARE.value -> R.drawable.ic_self_care
                        DbPurchases.TYPES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy
                        DbPurchases.TYPES.EDUCATION.value -> R.drawable.ic_school
                        DbPurchases.TYPES.DINING.value -> R.drawable.ic_restaurant
                        DbPurchases.TYPES.HEALTH.value -> R.drawable.ic_vaccines
                        DbPurchases.TYPES.TRANSPORTATION.value -> R.drawable.ic_directions_transit
                        DbPurchases.TYPES.MISCELLANEOUS.value -> R.drawable.ic_tag
                        else -> R.drawable.ic_tag
                    }
                )
            val types = holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemTypeTextView
                .context.resources.getStringArray(R.array.types)
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemTypeTextView
                .text = types[currentPurchase.type ?: DbPurchases.TYPES.MISCELLANEOUS.value]
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout
                .setOnLongClickListener {
                    listener.onItemInteraction(
                        ON_LONG_CLICK,
                        currentPurchase,
                        holder.getAdapterPosition()
                    )
                    true
                }

            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemPurchaseTypeIcon
                .setOnClickListener {
                    listener.onItemInteraction(
                        ON_BUTTON_CLICK,
                        currentPurchase,
                        holder.getAdapterPosition()
                    )
                }
        } else {
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout
                .setOnLongClickListener(null)
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemPurchaseTypeIcon
                .setOnClickListener(null)
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

    fun getLimit(increment: Boolean = false): Long {
        if (increment) currentLimit += 30
        return currentLimit
    }
}