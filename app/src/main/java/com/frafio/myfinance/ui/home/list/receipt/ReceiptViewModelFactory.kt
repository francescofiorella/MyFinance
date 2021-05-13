package com.frafio.myfinance.ui.home.list.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.data.repositories.ReceiptRepository

@Suppress("UNCHECKED_CAST")
class ReceiptViewModelFactory(
    private val repository: ReceiptRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ReceiptViewModel(repository) as T
    }
}