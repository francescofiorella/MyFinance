package com.frafio.myfinance.ui.home.list.receipt

import com.frafio.myfinance.data.models.ReceiptItem

interface ReceiptItemLongClickListener {
    fun onItemLongClick(receiptItem: ReceiptItem)
}