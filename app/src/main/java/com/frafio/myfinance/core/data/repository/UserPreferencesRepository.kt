package com.frafio.myfinance.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.frafio.myfinance.core.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private object PreferencesKeys {
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val MONTHLY_BUDGET = floatPreferencesKey("monthly_budget")
        val CURRENCY_CODE = stringPreferencesKey("currencyCode")
        val LABELS = stringSetPreferencesKey("labels")
        val USER_FULL_NAME = stringPreferencesKey("user_full_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHOTO_URL = stringPreferencesKey("user_photo_url")
        val USER_LOCAL_PHOTO_PATH = stringPreferencesKey("user_local_photo_path")
        val USER_PROVIDER = intPreferencesKey("user_provider")
        val USER_CREATION_YEAR = intPreferencesKey("user_creation_year")
        val USER_CREATION_MONTH = intPreferencesKey("user_creation_month")
        val USER_CREATION_DAY = intPreferencesKey("user_creation_day")
        val LAST_EXPENSES_SYNC = longPreferencesKey("last_expenses_sync")
        val LAST_INCOMES_SYNC = longPreferencesKey("last_incomes_sync")
        val LAST_EXPENSES_APP_SYNC = longPreferencesKey("last_expenses_app_sync")
        val LAST_INCOMES_APP_SYNC = longPreferencesKey("last_incomes_app_sync")
    }

    val userPreferencesFlow: StateFlow<UserPreferencesData> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
            val monthlyBudget = preferences[PreferencesKeys.MONTHLY_BUDGET]?.toDouble() ?: 0.0
            val currencyCode = preferences[PreferencesKeys.CURRENCY_CODE] ?: "EUR"
            val labels = preferences[PreferencesKeys.LABELS]?.toList()?.sorted() ?: emptyList()
            val lastExpensesSync = preferences[PreferencesKeys.LAST_EXPENSES_SYNC] ?: 0L
            val lastIncomesSync = preferences[PreferencesKeys.LAST_INCOMES_SYNC] ?: 0L
            val lastExpensesAppSync = preferences[PreferencesKeys.LAST_EXPENSES_APP_SYNC] ?: 0L
            val lastIncomesAppSync = preferences[PreferencesKeys.LAST_INCOMES_APP_SYNC] ?: 0L

            val email = preferences[PreferencesKeys.USER_EMAIL]
            val user = if (email != null) {
                User(
                    fullName = preferences[PreferencesKeys.USER_FULL_NAME],
                    email = email,
                    photoUrl = preferences[PreferencesKeys.USER_PHOTO_URL],
                    localPhotoPath = preferences[PreferencesKeys.USER_LOCAL_PHOTO_PATH],
                    provider = preferences[PreferencesKeys.USER_PROVIDER],
                    creationYear = preferences[PreferencesKeys.USER_CREATION_YEAR],
                    creationMonth = preferences[PreferencesKeys.USER_CREATION_MONTH],
                    creationDay = preferences[PreferencesKeys.USER_CREATION_DAY]
                )
            } else null

            UserPreferencesData(
                dynamicColor,
                monthlyBudget,
                currencyCode,
                labels,
                user,
                lastExpensesSync,
                lastIncomesSync,
                lastExpensesAppSync,
                lastIncomesAppSync
            )
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferencesData(
                dynamicColor = true,
                monthlyBudget = 0.0,
                currencyCode = "EUR",
                labels = emptyList(),
                user = null
            )
        )

    suspend fun updateDynamicColor(activate: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = activate
        }
    }

    suspend fun updateMonthlyBudget(budget: Double) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MONTHLY_BUDGET] = budget.toFloat()
        }
    }

    suspend fun updateCurrencyCode(currencyCode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_CODE] = currencyCode
        }
    }

    suspend fun updateLabels(labels: List<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LABELS] = labels.toSet()
        }
    }

    suspend fun updateLastExpensesSync(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_EXPENSES_SYNC] = timestamp
        }
    }

    suspend fun updateLastIncomesSync(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_INCOMES_SYNC] = timestamp
        }
    }

    suspend fun updateLastExpensesAppSync(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_EXPENSES_APP_SYNC] = timestamp
        }
    }

    suspend fun updateLastIncomesAppSync(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_INCOMES_APP_SYNC] = timestamp
        }
    }

    suspend fun resetSyncTimestamps() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_EXPENSES_SYNC] = 0L
            preferences[PreferencesKeys.LAST_INCOMES_SYNC] = 0L
            preferences[PreferencesKeys.LAST_EXPENSES_APP_SYNC] = 0L
            preferences[PreferencesKeys.LAST_INCOMES_APP_SYNC] = 0L
        }
    }

    suspend fun updateUser(user: User) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_FULL_NAME] = user.fullName ?: ""
            preferences[PreferencesKeys.USER_EMAIL] = user.email ?: ""
            preferences[PreferencesKeys.USER_PHOTO_URL] = user.photoUrl ?: ""
            preferences[PreferencesKeys.USER_LOCAL_PHOTO_PATH] = user.localPhotoPath ?: ""
            preferences[PreferencesKeys.USER_PROVIDER] = user.provider ?: User.EMAIL_PROVIDER
            preferences[PreferencesKeys.USER_CREATION_YEAR] = user.creationYear ?: 0
            preferences[PreferencesKeys.USER_CREATION_MONTH] = user.creationMonth ?: 0
            preferences[PreferencesKeys.USER_CREATION_DAY] = user.creationDay ?: 0
        }
    }

    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_FULL_NAME)
            preferences.remove(PreferencesKeys.USER_EMAIL)
            preferences.remove(PreferencesKeys.USER_PHOTO_URL)
            preferences.remove(PreferencesKeys.USER_LOCAL_PHOTO_PATH)
            preferences.remove(PreferencesKeys.USER_PROVIDER)
            preferences.remove(PreferencesKeys.USER_CREATION_YEAR)
            preferences.remove(PreferencesKeys.USER_CREATION_MONTH)
            preferences.remove(PreferencesKeys.USER_CREATION_DAY)
        }
    }
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
    val lastIncomesAppSync: Long = 0L
)
