package com.frafio.myfinance.ui.home.payments

import com.frafio.myfinance.data.models.Purchase

interface PurchaseInteractionListener {

    companion object{
        const val ON_CLICK: Int = 1
        const val ON_LONG_CLICK: Int = 2
        const val ON_BUTTON_CLICK: Int = 3
        const val ON_HALF_LIST_PASSED: Int = 4
    }

    fun onItemInteraction(interactionID: Int, purchase: Purchase, position: Int)
}