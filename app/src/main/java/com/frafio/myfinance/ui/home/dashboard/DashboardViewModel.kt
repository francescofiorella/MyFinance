package com.frafio.myfinance.ui.home.dashboard

import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.repositories.DataRepository

class DashboardViewModel(
    repository: DataRepository
) : ViewModel() {
    val dayAvgString: String = repository.calculateStats()[0]
    val monthAvgString: String = repository.calculateStats()[1]
    val todayTotString: String = repository.calculateStats()[2]
    val totString: String = repository.calculateStats()[3]
    val numTotString: String = repository.calculateStats()[4]
    val ticketTotString: String = repository.calculateStats()[5]
    val trenTotString: String = repository.calculateStats()[6]
    val amTotString: String = repository.calculateStats()[7]

    val warningVisibility = repository.dashboardWarningVisibility()
    val statsVisibility = repository.dashboardStatsVisibility()
}