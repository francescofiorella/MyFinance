package com.frafio.myfinance.ui.home.expenses

import com.frafio.myfinance.data.model.Expense

interface ExpenseInteractionListener {

    companion object{
        const val ON_CLICK: Int = 1
        const val ON_LONG_CLICK: Int = 2
        const val ON_BUTTON_CLICK: Int = 3
        const val ON_LOAD_MORE_REQUEST: Int = 4
    }

    fun onItemInteraction(interactionID: Int, expense: Expense, position: Int)
}