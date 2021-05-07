package com.frafio.myfinance.ui.home.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.data.repositories.StatsRepository

@Suppress("UNCHECKED_CAST")
class DashboardViewModelFactory(
        private val repository: StatsRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DashboardViewModel(repository) as T
    }
}