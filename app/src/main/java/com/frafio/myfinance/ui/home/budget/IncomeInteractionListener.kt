package com.frafio.myfinance.ui.home.budget

import com.frafio.myfinance.data.models.Purchase

interface IncomeInteractionListener {

    companion object {
        const val ON_CLICK: Int = 1
        const val ON_LONG_CLICK: Int = 2
        const val ON_LOAD_MORE_REQUEST: Int = 3
    }

    fun onItemInteraction(interactionID: Int, purchase: Purchase, position: Int)
}