package com.frafio.myfinance.core.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A category or label filter requested from another screen, waiting for the Expenses tab to apply it.
 * It holds the value until [clear], so the tab picks it up whether or not its ViewModel exists yet;
 * a new request replaces the pending one.
 */
@Singleton
class PendingExpensesFilter @Inject constructor() {
    private val _categories = MutableStateFlow<List<Int>?>(null)
    val categories: StateFlow<List<Int>?> = _categories.asStateFlow()

    private val _labels = MutableStateFlow<List<String>?>(null)
    val labels: StateFlow<List<String>?> = _labels.asStateFlow()

    fun request(categoryId: Int) {
        _labels.value = null
        _categories.value = listOf(categoryId)
    }

    fun requestLabel(label: String) {
        _categories.value = null
        _labels.value = listOf(label)
    }

    fun clear() {
        _categories.value = null
        _labels.value = null
    }
}
