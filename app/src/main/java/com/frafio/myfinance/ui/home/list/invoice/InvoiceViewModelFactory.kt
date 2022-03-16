package com.frafio.myfinance.ui.home.list.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.data.repositories.InvoiceRepository

@Suppress("UNCHECKED_CAST")
class InvoiceViewModelFactory(
    private val repository: InvoiceRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InvoiceViewModel(repository) as T
    }
}