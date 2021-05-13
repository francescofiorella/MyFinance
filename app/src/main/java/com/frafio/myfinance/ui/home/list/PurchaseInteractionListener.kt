package com.frafio.myfinance.ui.home.list

import com.frafio.myfinance.data.models.Purchase

interface PurchaseInteractionListener {
    // 1: onClick, 2: onLongClick
    fun onItemInteraction(interactionID: Int, purchase: Purchase, position: Int)
}