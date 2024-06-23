package com.frafio.myfinance.ui.home.payments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.managers.PurchaseManager.Companion.DEFAULT_LIMIT
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.databinding.LayoutPurchaseItemRvBinding
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_BUTTON_CLICK
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_LOAD_MORE_REQUEST
import com.frafio.myfinance.ui.home.payments.PurchaseInteractionListener.Companion.ON_LONG_CLICK

class PurchaseAdapter(
    private var purchases: List<Purchase>,
    private val listener: PurchaseInteractionListener
) : RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder>() {

    private var currentLimit: Long = DEFAULT_LIMIT

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

        if (purchases.size - position < DEFAULT_LIMIT) {
            listener.onItemInteraction(ON_LOAD_MORE_REQUEST, currentPurchase, position)
        }

        if (currentPurchase.category != DbPurchases.CATEGORIES.TOTAL.value) {
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemPurchaseCategoryIcon.icon =
                ContextCompat.getDrawable(
                    holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemPurchaseCategoryIcon.context,
                    when (purchases[position].category) {
                        DbPurchases.CATEGORIES.HOUSING.value -> R.drawable.ic_baseline_home
                        DbPurchases.CATEGORIES.GROCERIES.value -> R.drawable.ic_shopping_cart
                        DbPurchases.CATEGORIES.PERSONAL_CARE.value -> R.drawable.ic_self_care
                        DbPurchases.CATEGORIES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy
                        DbPurchases.CATEGORIES.EDUCATION.value -> R.drawable.ic_school
                        DbPurchases.CATEGORIES.DINING.value -> R.drawable.ic_restaurant
                        DbPurchases.CATEGORIES.HEALTH.value -> R.drawable.ic_vaccines
                        DbPurchases.CATEGORIES.TRANSPORTATION.value -> R.drawable.ic_directions_transit
                        DbPurchases.CATEGORIES.MISCELLANEOUS.value -> R.drawable.ic_tag
                        else -> R.drawable.ic_tag
                    }
                )
            val types = holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemCategoryTextView
                .context.resources.getStringArray(R.array.categories)
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemCategoryTextView
                .text =
                types[currentPurchase.category ?: DbPurchases.CATEGORIES.MISCELLANEOUS.value]
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemConstraintLayout
                .setOnLongClickListener {
                    listener.onItemInteraction(
                        ON_LONG_CLICK,
                        currentPurchase,
                        holder.getAdapterPosition()
                    )
                    true
                }

            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemPurchaseCategoryIcon
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
            holder.recyclerViewPurchaseItemBinding.recViewPurchaseItemPurchaseCategoryIcon
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
        if (increment) currentLimit += DEFAULT_LIMIT
        return currentLimit
    }
}