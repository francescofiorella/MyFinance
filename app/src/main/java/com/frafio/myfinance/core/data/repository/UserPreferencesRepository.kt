package com.frafio.myfinance.core.data.repository

import com.frafio.myfinance.core.data.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserPreferencesRepository {

    val userPreferencesFlow: StateFlow<UserPreferencesData>

    suspend fun updateProPicChoice(choice: String)

    suspend fun updateDynamicColor(activate: Boolean)

    suspend fun updateMonthlyBudget(budget: Double)

    suspend fun updateCurrencyCode(currencyCode: String)

    suspend fun updateLabels(labels: List<String>)

    suspend fun updateLastExpensesSync(timestamp: Long)

    suspend fun updateLastIncomesSync(timestamp: Long)

    suspend fun updateLastExpensesAppSync(timestamp: Long)

    suspend fun updateLastIncomesAppSync(timestamp: Long)

    suspend fun resetSyncTimestamps()

    suspend fun updateUser(user: User)

    suspend fun clearUserData()
}

data class UserPreferencesData(
    val dynamicColor: Boolean,
    val monthlyBudget: Double,
    val currencyCode: String,
    val labels: List<String>,
    val user: User?,
    val lastExpensesSync: Long = 0L,
    val lastIncomesSync: Long = 0L,
    val lastExpensesAppSync: Long = 0L,
    val lastIncomesAppSync: Long = 0L,
    val proPicChoice: String? = null
)
