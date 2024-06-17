package com.frafio.myfinance.ui.home.payments

import androidx.recyclerview.widget.DiffUtil
import com.frafio.myfinance.data.models.Purchase

class PurchaseDiffUtil(
    private val oldPurchaseList: List<Purchase>,
    private val newPurchaseList: List<Purchase>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldPurchaseList.size
    }

    override fun getNewListSize(): Int {
        return newPurchaseList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldPurchaseList[oldItemPosition].id == newPurchaseList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldPurchaseList[oldItemPosition].email != newPurchaseList[newItemPosition].email -> false

            oldPurchaseList[oldItemPosition].name != newPurchaseList[newItemPosition].name -> false

            oldPurchaseList[oldItemPosition].price != newPurchaseList[newItemPosition].price -> false

            oldPurchaseList[oldItemPosition].year != newPurchaseList[newItemPosition].year -> false

            oldPurchaseList[oldItemPosition].month != newPurchaseList[newItemPosition].month -> false

            oldPurchaseList[oldItemPosition].day != newPurchaseList[newItemPosition].day -> false

            oldPurchaseList[oldItemPosition].type != newPurchaseList[newItemPosition].type -> false

            oldPurchaseList[oldItemPosition].category != newPurchaseList[newItemPosition].category -> false

            else -> true
        }
    }
}