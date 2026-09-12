package com.frafio.myfinance.testing.repository

import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.core.data.repository.UserPreferencesData
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.testing.data.testPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Backed by a [MutableStateFlow] because the production interface exposes a [StateFlow]
 * that ViewModels read synchronously at construction. Every `update*` really mutates it.
 */
class TestUserPreferencesRepository : UserPreferencesRepository {

    private val preferences = MutableStateFlow(testPreferences())

    override val userPreferencesFlow: StateFlow<UserPreferencesData> = preferences

    var clearUserDataCount = 0
        private set

    override suspend fun updateProPicChoice(choice: String) {
        preferences.update { it.copy(proPicChoice = choice) }
    }

    override suspend fun updateDynamicColor(activate: Boolean) {
        preferences.update { it.copy(dynamicColor = activate) }
    }

    override suspend fun updateMonthlyBudget(budget: Double) {
        preferences.update { it.copy(monthlyBudget = budget) }
    }

    override suspend fun updateCurrencyCode(currencyCode: String) {
        preferences.update { it.copy(currencyCode = currencyCode) }
    }

    override suspend fun updateLabels(labels: List<String>) {
        preferences.update { it.copy(labels = labels.toSet().sorted()) }
    }

    override suspend fun updateLastExpensesSync(timestamp: Long) {
        preferences.update { it.copy(lastExpensesSync = timestamp) }
    }

    override suspend fun updateLastIncomesSync(timestamp: Long) {
        preferences.update { it.copy(lastIncomesSync = timestamp) }
    }

    override suspend fun updateLastExpensesAppSync(timestamp: Long) {
        preferences.update { it.copy(lastExpensesAppSync = timestamp) }
    }

    override suspend fun updateLastIncomesAppSync(timestamp: Long) {
        preferences.update { it.copy(lastIncomesAppSync = timestamp) }
    }

    override suspend fun resetSyncTimestamps() {
        preferences.update {
            it.copy(
                lastExpensesSync = 0L,
                lastIncomesSync = 0L,
                lastExpensesAppSync = 0L,
                lastIncomesAppSync = 0L,
            )
        }
    }

    override suspend fun updateUser(user: User) {
        preferences.update { it.copy(user = user) }
    }

    override suspend fun clearUserData() {
        clearUserDataCount++
        preferences.update { it.copy(user = null) }
    }

    /**
     * A test-only API to allow controlling the stored preferences from tests.
     */
    fun setPreferences(data: UserPreferencesData) {
        preferences.value = data
    }

    fun setLabels(labels: List<String>) = preferences.update { it.copy(labels = labels) }

    fun setMonthlyBudget(budget: Double) = preferences.update { it.copy(monthlyBudget = budget) }

    fun setCurrencyCode(currencyCode: String) = preferences.update { it.copy(currencyCode = currencyCode) }

    fun setUser(user: User?) = preferences.update { it.copy(user = user) }
}
