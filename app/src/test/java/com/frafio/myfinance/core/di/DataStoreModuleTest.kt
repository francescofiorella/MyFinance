package com.frafio.myfinance.core.di

import android.content.Context
import androidx.core.content.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.frafio.myfinance.core.data.repository.UserPreferencesRepositoryImpl
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * One test: DataStore allows a single active instance per file per process, and this opens the
 * production file. The legacy `SHARED_PREFERENCES` migration was dropped (its keys had been
 * renamed away), so an old file is neither read nor removed.
 */
@RunWith(RobolectricTestRunner::class)
class DataStoreModuleTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun provideDataStore_persistsTheUserPreferencesFile_andIgnoresLegacyPreferences() = runTest {
        val legacy = context.getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE)
        legacy.edit(commit = true) {
            putFloat("MONTHLY_BUDGET_KEY", 999f)
            putStringSet("LABELS_KEY", setOf("Old"))
        }

        val dataStore = DataStoreModule.provideDataStore(context)
        val repository = UserPreferencesRepositoryImpl(dataStore)

        val before = dataStore.data.first()
        assertThat(before[floatPreferencesKey("monthly_budget")]).isNull()
        assertThat(before.asMap()).isEmpty()

        repository.updateMonthlyBudget(123.45)
        repository.updateLabels(listOf("Work", "Dinner"))
        repository.updateUser(testUser())

        val stored = dataStore.data.first()
        assertThat(stored[floatPreferencesKey("monthly_budget")]).isWithin(0.001f).of(123.45f)
        assertThat(stored[stringSetPreferencesKey("labels")]).containsExactly("Work", "Dinner")
        assertThat(stored[stringPreferencesKey("user_email")]).isEqualTo("ada@example.com")
        assertThat(context.preferencesDataStoreFile("user_preferences").exists()).isTrue()

        // The old file is left alone.
        assertThat(legacy.getFloat("MONTHLY_BUDGET_KEY", 0f)).isEqualTo(999f)
    }
}
