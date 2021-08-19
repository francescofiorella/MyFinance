package com.frafio.myfinance.ui.home.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.data.repositories.UserRepository

class DashboardViewModel(
    private val purchaseRepository: PurchaseRepository,
    userRepository: UserRepository
) : ViewModel() {
    val proPic: String? = userRepository.getProPic()

    private val _purchaseListSize = MutableLiveData<Int>()
    val purchaseListSize: LiveData<Int>
        get() = _purchaseListSize

    private val _dayAvgString = MutableLiveData<String>()
    val dayAvgString: LiveData<String>
        get() = _dayAvgString

    private val _monthAvgString = MutableLiveData<String>()
    val monthAvgString: LiveData<String>
        get() = _monthAvgString

    private val _todayTotString = MutableLiveData<String>()
    val todayTotString: LiveData<String>
        get() = _todayTotString

    private val _totString = MutableLiveData<String>()
    val totString: LiveData<String>
        get() = _totString

    private val _numTotString = MutableLiveData<String>()
    val numTotString: LiveData<String>
        get() = _numTotString

    private val _ticketTotString = MutableLiveData<String>()
    val ticketTotString: LiveData<String>
        get() = _ticketTotString

    private val _trenTotString = MutableLiveData<String>()
    val trenTotString: LiveData<String>
        get() = _trenTotString

    private val _amTotString = MutableLiveData<String>()
    val amTotString: LiveData<String>
        get() = _amTotString

    fun getStats() {
        val stats = purchaseRepository.calculateStats()
        _purchaseListSize.value = purchaseRepository.purchaseListSize()
        _dayAvgString.value = stats[0]
        _monthAvgString.value = stats[1]
        _todayTotString.value = stats[2]
        _totString.value = stats[3]
        _numTotString.value = stats[4]
        _ticketTotString.value = stats[5]
        _trenTotString.value = stats[6]
        _amTotString.value = stats[7]
    }
}