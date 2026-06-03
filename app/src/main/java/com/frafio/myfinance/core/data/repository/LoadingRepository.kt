package com.frafio.myfinance.core.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadingRepository @Inject constructor() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val loadingCount = AtomicInteger(0)

    fun startLoading() {
        loadingCount.incrementAndGet()
        updateLoadingState()
    }

    fun stopLoading() {
        if (loadingCount.get() > 0) {
            loadingCount.decrementAndGet()
        }
        updateLoadingState()
    }

    private fun updateLoadingState() {
        _isLoading.value = loadingCount.get() > 0
    }
}
