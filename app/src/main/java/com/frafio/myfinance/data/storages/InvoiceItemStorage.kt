package com.frafio.myfinance.data.storages

import com.frafio.myfinance.data.models.InvoiceItem

object InvoiceItemStorage {
    private var invoiceItemList: MutableList<InvoiceItem> = mutableListOf()

    fun setList(newList: MutableList<InvoiceItem>) {
        invoiceItemList = newList
    }

    fun getList(): List<InvoiceItem> {
        return invoiceItemList
    }

    fun listIndexOf(invoiceItem: InvoiceItem): Int {
        return invoiceItemList.indexOf(invoiceItem)
    }

    fun addToList(invoiceItem: InvoiceItem) {
        invoiceItemList.add(invoiceItem)
        invoiceItemList.sortBy { it.name }
    }

    fun removeFromList(invoiceItem: InvoiceItem) {
        invoiceItemList.remove(invoiceItem)
    }

    fun resetList() {
        invoiceItemList = mutableListOf()
    }
}