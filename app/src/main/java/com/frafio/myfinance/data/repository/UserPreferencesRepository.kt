package com.frafio.myfinance.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val DYNAMIC_COLOR = booleanPreferencesKey(MyFinanceApplication.DYNAMIC_COLOR_KEY)
        val MONTHLY_BUDGET = floatPreferencesKey(MyFinanceApplication.MONTHLY_BUDGET_KEY)
        val LABELS = stringSetPreferencesKey(MyFinanceApplication.LABELS_KEY)
        val USER_FULL_NAME = stringPreferencesKey("user_full_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHOTO_URL = stringPreferencesKey("user_photo_url")
        val USER_PROVIDER = intPreferencesKey("user_provider")
        val USER_CREATION_YEAR = intPreferencesKey("user_creation_year")
        val USER_CREATION_MONTH = intPreferencesKey("user_creation_month")
        val USER_CREATION_DAY = intPreferencesKey("user_creation_day")
    }

    val userPreferencesFlow: Flow<UserPreferencesData> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: false
            val monthlyBudget = preferences[PreferencesKeys.MONTHLY_BUDGET]?.toDouble() ?: 0.0
            val labels = preferences[PreferencesKeys.LABELS]?.toList()?.sorted() ?: emptyList()

            val email = preferences[PreferencesKeys.USER_EMAIL]
            val user = if (email != null) {
                User(
                    fullName = preferences[PreferencesKeys.USER_FULL_NAME],
                    email = email,
                    photoUrl = preferences[PreferencesKeys.USER_PHOTO_URL],
                    provider = preferences[PreferencesKeys.USER_PROVIDER],
                    creationYear = preferences[PreferencesKeys.USER_CREATION_YEAR],
                    creationMonth = preferences[PreferencesKeys.USER_CREATION_MONTH],
                    creationDay = preferences[PreferencesKeys.USER_CREATION_DAY]
                )
            } else null

            UserPreferencesData(dynamicColor, monthlyBudget, labels, user)
        }

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

    suspend fun updateLabels(labels: List<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LABELS] = labels.toSet()
        }
    }

    suspend fun updateUser(user: User) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_FULL_NAME] = user.fullName ?: ""
            preferences[PreferencesKeys.USER_EMAIL] = user.email ?: ""
            preferences[PreferencesKeys.USER_PHOTO_URL] = user.photoUrl ?: ""
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
    val labels: List<String>,
    val user: User?
)
