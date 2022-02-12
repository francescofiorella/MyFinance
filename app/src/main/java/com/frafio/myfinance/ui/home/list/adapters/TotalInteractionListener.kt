package com.frafio.myfinance.ui.home.list.adapters

import com.frafio.myfinance.data.models.Purchase

interface TotalInteractionListener {

    companion object{
        const val ON_CLICK: Int = 1
        const val ON_LONG_CLICK: Int = 2
    }

    fun onItemInteraction(interactionID: Int, purchase: Purchase, position: Int)
}