package com.frafio.myfinance.core.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A category filter requested from another screen, waiting for the Expenses tab to apply it.
 * It holds the value until [clear], so the tab picks it up whether or not its ViewModel exists yet.
 */
@Singleton
class PendingExpensesFilter @Inject constructor() {
    private val _categories = MutableStateFlow<List<Int>?>(null)
    val categories: StateFlow<List<Int>?> = _categories.asStateFlow()

    fun request(categoryId: Int) {
        _categories.value = listOf(categoryId)
    }

    fun clear() {
        _categories.value = null
    }
}
