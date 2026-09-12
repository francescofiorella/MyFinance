package com.frafio.myfinance.core.data.repository

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.util.InMemoryDataStore
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UserPreferencesRepositoryTest {

    // The impl launches stateIn(Eagerly) on Dispatchers.Main in its constructor.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var dataStore: InMemoryDataStore<Preferences>
    private lateinit var subject: UserPreferencesRepositoryImpl

    @Before
    fun setup() {
        dataStore = InMemoryDataStore(emptyPreferences())
        subject = UserPreferencesRepositoryImpl(dataStore)
    }

    @Test
    fun emptyStore_yieldsDefaults() = testScope.runTest {
        val data = subject.userPreferencesFlow.first()

        assertThat(data.dynamicColor).isTrue()
        assertThat(data.currencyCode).isEqualTo("EUR")
        assertThat(data.monthlyBudget).isEqualTo(0.0)
        assertThat(data.labels).isEmpty()
        assertThat(data.user).isNull()
        assertThat(data.proPicChoice).isNull()
        assertThat(data.lastExpensesSync).isEqualTo(0L)
        assertThat(data.lastIncomesSync).isEqualTo(0L)
        assertThat(data.lastExpensesAppSync).isEqualTo(0L)
        assertThat(data.lastIncomesAppSync).isEqualTo(0L)
    }

    @Test
    fun initialValue_matchesTheDefaultsSynchronously() {
        // Seven ViewModels read .value at construction as their own stateIn initial value.
        val initial = subject.userPreferencesFlow.value

        assertThat(initial.dynamicColor).isTrue()
        assertThat(initial.currencyCode).isEqualTo("EUR")
        assertThat(initial.monthlyBudget).isEqualTo(0.0)
        assertThat(initial.user).isNull()
    }

    @Test
    fun updateCurrencyCode_roundTrips() = testScope.runTest {
        subject.updateCurrencyCode("USD")
        assertThat(subject.userPreferencesFlow.first().currencyCode).isEqualTo("USD")
    }

    @Test
    fun updateDynamicColor_roundTrips() = testScope.runTest {
        subject.updateDynamicColor(false)
        assertThat(subject.userPreferencesFlow.first().dynamicColor).isFalse()
    }

    @Test
    fun updateProPicChoice_roundTrips() = testScope.runTest {
        subject.updateProPicChoice("avatar_3")
        assertThat(subject.userPreferencesFlow.first().proPicChoice).isEqualTo("avatar_3")
    }

    @Test
    fun updateMonthlyBudget_roundTripsThroughFloatPrecision() = testScope.runTest {
        subject.updateMonthlyBudget(1234.56)
        // Stored via floatPreferencesKey, so the Double comes back with Float precision.
        assertThat(subject.userPreferencesFlow.first().monthlyBudget).isWithin(0.01).of(1234.56)
    }

    @Test
    fun updateLabels_returnsThemSortedCaseSensitively() = testScope.runTest {
        subject.updateLabels(listOf("zeta", "alpha", "Mid"))
        assertThat(subject.userPreferencesFlow.first().labels)
            .containsExactly("Mid", "alpha", "zeta").inOrder()
    }

    @Test
    fun updateLabels_collapsesDuplicates() = testScope.runTest {
        subject.updateLabels(listOf("a", "a", "b"))
        assertThat(subject.userPreferencesFlow.first().labels).containsExactly("a", "b").inOrder()
    }

    @Test
    fun updateUser_roundTripsEveryField() = testScope.runTest {
        val user = testUser(
            email = "ada@example.com",
            fullName = "Ada",
            photoUrl = "https://example.com/p.png",
            provider = User.GOOGLE_PROVIDER,
            providers = listOf("google.com", "password"),
            hasPassword = true,
            isGoogleLinked = true,
        ).copy(localPhotoPath = "/tmp/p.png")

        subject.updateUser(user)

        assertThat(subject.userPreferencesFlow.first().user).isEqualTo(user)
    }

    @Test
    fun updateUser_storesNullsAsEmptyStringsAndZeros() = testScope.runTest {
        subject.updateUser(User(email = "ada@example.com"))

        val stored = subject.userPreferencesFlow.first().user
        assertThat(stored).isNotNull()
        assertThat(stored!!.fullName).isEmpty()
        assertThat(stored.photoUrl).isEmpty()
        assertThat(stored.localPhotoPath).isEmpty()
        assertThat(stored.provider).isEqualTo(User.EMAIL_PROVIDER)
        assertThat(stored.creationYear).isEqualTo(0)
        assertThat(stored.creationMonth).isEqualTo(0)
        assertThat(stored.creationDay).isEqualTo(0)
    }

    @Test
    fun updateUser_withNullEmail_stillMaterialisesAUserRow() = testScope.runTest {
        subject.updateUser(User(fullName = "Nameless", email = null))

        // email is written as "" and "" != null, so a user row still materialises.
        assertThat(subject.userPreferencesFlow.first().user).isNotNull()

        subject.clearUserData()
        assertThat(subject.userPreferencesFlow.first().user).isNull()
    }

    @Test
    fun clearUserData_removesOnlyTheUser() = testScope.runTest {
        subject.updateUser(testUser())
        subject.updateLabels(listOf("keep"))
        subject.updateCurrencyCode("GBP")
        subject.updateMonthlyBudget(500.0)
        subject.updateProPicChoice("avatar_1")
        subject.updateLastExpensesSync(42L)

        subject.clearUserData()

        val data = subject.userPreferencesFlow.first()
        assertThat(data.user).isNull()
        assertThat(data.labels).containsExactly("keep")
        assertThat(data.currencyCode).isEqualTo("GBP")
        assertThat(data.monthlyBudget).isWithin(0.01).of(500.0)
        assertThat(data.proPicChoice).isEqualTo("avatar_1")
        assertThat(data.lastExpensesSync).isEqualTo(42L)
    }

    @Test
    fun syncTimestamps_roundTripIndependently() = testScope.runTest {
        subject.updateLastExpensesSync(1L)
        subject.updateLastIncomesSync(2L)
        subject.updateLastExpensesAppSync(3L)
        subject.updateLastIncomesAppSync(4L)

        val data = subject.userPreferencesFlow.first()
        assertThat(data.lastExpensesSync).isEqualTo(1L)
        assertThat(data.lastIncomesSync).isEqualTo(2L)
        assertThat(data.lastExpensesAppSync).isEqualTo(3L)
        assertThat(data.lastIncomesAppSync).isEqualTo(4L)
    }

    @Test
    fun resetSyncTimestamps_zeroesAllFour() = testScope.runTest {
        subject.updateLastExpensesSync(1L)
        subject.updateLastIncomesSync(2L)
        subject.updateLastExpensesAppSync(3L)
        subject.updateLastIncomesAppSync(4L)

        subject.resetSyncTimestamps()

        val data = subject.userPreferencesFlow.first()
        assertThat(data.lastExpensesSync).isEqualTo(0L)
        assertThat(data.lastIncomesSync).isEqualTo(0L)
        assertThat(data.lastExpensesAppSync).isEqualTo(0L)
        assertThat(data.lastIncomesAppSync).isEqualTo(0L)
    }

    @Test
    fun updates_areVisibleOnTheStateFlowValue() = testScope.runTest {
        subject.updateCurrencyCode("JPY")
        assertThat(subject.userPreferencesFlow.value.currencyCode).isEqualTo("JPY")
    }
}
