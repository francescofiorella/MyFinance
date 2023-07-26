package com.frafio.myfinance.ui.home.payments.invoice

import com.frafio.myfinance.data.models.InvoiceItem

interface InvoiceItemLongClickListener {
    fun onItemLongClick(invoiceItem: InvoiceItem)
}