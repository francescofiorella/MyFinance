package com.frafio.myfinance.ui.home.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.data.repositories.DataRepository

@Suppress("UNCHECKED_CAST")
class DashboardViewModelFactory(
        private val repository: DataRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DashboardViewModel(repository) as T
    }
}