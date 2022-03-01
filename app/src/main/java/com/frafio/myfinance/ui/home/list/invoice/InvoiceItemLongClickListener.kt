package com.frafio.myfinance.ui.home.list.invoice

import com.frafio.myfinance.data.models.InvoiceItem

interface InvoiceItemLongClickListener {
    fun onItemLongClick(invoiceItem: InvoiceItem)
}