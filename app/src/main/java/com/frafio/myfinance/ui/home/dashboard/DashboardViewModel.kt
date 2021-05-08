package com.frafio.myfinance.ui.home.dashboard

import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.repositories.PurchaseRepository

class DashboardViewModel(
    repository: PurchaseRepository
) : ViewModel() {
    private val stats = repository.calculateStats()

    val dayAvgString = stats[0]
    val monthAvgString = stats[1]
    val todayTotString = stats[2]
    val totString: String = stats[3]
    val numTotString: String = stats[4]
    val ticketTotString: String = stats[5]
    val trenTotString: String = stats[6]
    val amTotString: String = stats[7]

    val warningVisibility = repository.warningVisibility()
    val statsVisibility = repository.purchaseVisibility()
}