package com.frafio.myfinance.ui.home.list

import com.frafio.myfinance.data.models.Purchase

interface RecyclerViewInteractionListener {
    // 1: onClick, 2: onLongClick
    fun onRecyclerViewItemInteraction(interactionID: Int, purchase: Purchase, position: Int)
}