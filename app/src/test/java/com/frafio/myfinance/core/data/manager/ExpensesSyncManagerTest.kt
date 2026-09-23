package com.frafio.myfinance.core.data.manager

import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepositoryImpl
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.data.testPreferences
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.remote.TestRemoteDataSource
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.util.DatabaseTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

/** The expense-specific behavior: user-root fields, labels, and the root listener. */
@RunWith(RobolectricTestRunner::class)
class ExpensesSyncManagerTest : DatabaseTest() {

    private val collection = FirestoreEnums.FIELDS.PAYMENTS.value
    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private lateinit var preferences: TestUserPreferencesRepository
    private lateinit var remote: TestRemoteDataSource
    private lateinit var subject: ExpensesSyncManager

    private val prefs get() = preferences.userPreferencesFlow.value

    @Before
    fun setup() {
        preferences = TestUserPreferencesRepository().apply {
            setPreferences(testPreferences(user = testUser(), labels = listOf("Dinner", "Work")))
        }
        remote = TestRemoteDataSource()
        subject = ExpensesSyncManager(preferences, ExpensesLocalRepositoryImpl(expenseDao), db, expenseDao, remote, dispatcher)
    }

    @After
    fun teardown() {
        scope.cancel()
    }

    // region user-root fields

    @Test
    fun setMonthlyBudget_mergesRootField_andUpdatesPreferences() = runBlocking {
        val result = subject.setMonthlyBudget(750.0)

        assertThat(result.code).isEqualTo(FinanceCode.BUDGET_UPDATE_SUCCESS.code)
        assertThat(remote.mergedUserFields).containsExactly(mapOf(FirestoreEnums.FIELDS.MONTHLY_BUDGET.value to 750.0))
        assertThat(prefs.monthlyBudget).isEqualTo(750.0)
    }

    @Test
    fun setMonthlyBudget_remoteFailure_returnsFailure_andLeavesPreferences() = runBlocking {
        remote.failNext = IOException("offline")

        val result = subject.setMonthlyBudget(750.0)

        assertThat(result.code).isEqualTo(FinanceCode.BUDGET_UPDATE_FAILURE.code)
        assertThat(prefs.monthlyBudget).isEqualTo(0.0)
    }

    @Test
    fun setCurrencyCode_mergesRootField_andUpdatesPreferences() = runBlocking {
        val result = subject.setCurrencyCode("USD")

        assertThat(result.code).isEqualTo(FinanceCode.BUDGET_UPDATE_SUCCESS.code)
        assertThat(remote.mergedUserFields).containsExactly(mapOf(FirestoreEnums.FIELDS.CURRENCY_CODE.value to "USD"))
        assertThat(prefs.currencyCode).isEqualTo("USD")
    }

    @Test
    fun setProPicChoice_mergesRootField_andUpdatesPreferences() = runBlocking {
        val result = subject.setProPicChoice("cuate")

        assertThat(result.code).isEqualTo(FinanceCode.BUDGET_UPDATE_SUCCESS.code)
        assertThat(remote.mergedUserFields).containsExactly(mapOf(FirestoreEnums.FIELDS.PRO_PIC_CHOICE.value to "cuate"))
        assertThat(prefs.proPicChoice).isEqualTo("cuate")
    }

    @Test
    fun setLabels_sortsThem_mergesRootField_andUpdatesPreferences() = runBlocking {
        val result = subject.setLabels(listOf("Work", "Dinner", "Gift"))

        assertThat(result.code).isEqualTo(FinanceCode.LABELS_UPDATE_SUCCESS.code)
        assertThat(remote.mergedUserFields).containsExactly(mapOf(FirestoreEnums.FIELDS.LABELS.value to listOf("Dinner", "Gift", "Work")))
        assertThat(prefs.labels).containsExactly("Dinner", "Gift", "Work").inOrder()
    }

    @Test
    fun setLabels_withoutUser_fails() = runBlocking {
        preferences.setUser(null)

        val result = subject.setLabels(listOf("Gift"))

        assertThat(result.code).isEqualTo(FinanceCode.LABELS_UPDATE_FAILURE.code)
        assertThat(remote.mergedUserFields).isEmpty()
    }

    // endregion

    // region labels

    @Test
    fun addLabel_trimsAndAppends() = runBlocking {
        val result = subject.addLabel("  Gift ")

        assertThat(result.code).isEqualTo(FinanceCode.LABEL_ADD_SUCCESS.code)
        assertThat(prefs.labels).containsExactly("Dinner", "Gift", "Work").inOrder()
    }

    @Test
    fun addLabel_rejectsBlankAndDuplicates() = runBlocking {
        assertThat(subject.addLabel("   ").code).isEqualTo(FinanceCode.LABELS_UPDATE_FAILURE.code)
        assertThat(subject.addLabel("Dinner").code).isEqualTo(FinanceCode.LABELS_UPDATE_FAILURE.code)
        assertThat(remote.mergedUserFields).isEmpty()
    }

    @Test
    fun updateExpenseLabels_updatesRemoteArray_andLocalRow() = runBlocking {
        onIo { expenseDao.upsert(testExpense(name = "Pizza", id = "p", labels = listOf("Dinner"))) }

        val result = subject.updateExpenseLabels("p", "Work", isAddition = true)

        assertThat(result.code).isEqualTo(FinanceCode.EXPENSE_EDIT_SUCCESS.code)
        val update = remote.arrayUpdates.single()
        assertThat(update.id).isEqualTo("p")
        assertThat(update.field).isEqualTo(FirestoreEnums.FIELDS.LABELS.value)
        assertThat(update.add).isTrue()
        val local = expenseDao.getById("p")!!
        assertThat(local.labels).containsExactly("Dinner", "Work")
        assertThat(local.updatedAt).isEqualTo(update.updatedAt)
    }

    @Test
    fun updateExpenseLabels_removal_dropsTheLabelLocally() = runBlocking {
        onIo { expenseDao.upsert(testExpense(name = "Pizza", id = "p", labels = listOf("Dinner", "Work"))) }

        subject.updateExpenseLabels("p", "Work", isAddition = false)

        assertThat(expenseDao.getById("p")!!.labels).containsExactly("Dinner")
        assertThat(remote.arrayUpdates.single().add).isFalse()
    }

    @Test
    fun updateExpenseLabels_remoteFailure_leavesTheRow() = runBlocking<Unit> {
        onIo { expenseDao.upsert(testExpense(name = "Pizza", id = "p", labels = listOf("Dinner"))) }
        remote.failNext = IOException("offline")

        val result = subject.updateExpenseLabels("p", "Work", isAddition = true)

        assertThat(result.code).isEqualTo(FinanceCode.EXPENSE_EDIT_FAILURE.code)
        assertThat(expenseDao.getById("p")!!.labels).containsExactly("Dinner")
    }

    @Test
    fun deleteLabel_removesItFromEveryExpense_andReportsThem() = runBlocking<Unit> {
        onIo { expenseDao.upsert(testExpense(name = "Pizza", id = "p", labels = listOf("Dinner", "Work"))) }
        onIo { expenseDao.upsert(testExpense(name = "Bus", id = "b", labels = listOf("Work"))) }
        onIo { expenseDao.upsert(testExpense(name = "Milk", id = "m")) }

        val result = subject.deleteLabel("Work")

        assertThat(result.financeResult.code).isEqualTo(FinanceCode.LABEL_DELETE_SUCCESS.code)
        assertThat(result.affectedExpenses.map { it.id }).containsExactly("p", "b")
        assertThat(prefs.labels).containsExactly("Dinner")
        assertThat(expenseDao.getById("p")!!.labels).containsExactly("Dinner")
        assertThat(expenseDao.getById("b")!!.labels).isEmpty()
        assertThat(remote.sets.map { it.second }).containsExactly("p", "b")
    }

    @Test
    fun deleteLabel_unknownLabel_fails() = runBlocking {
        val result = subject.deleteLabel("Gift")

        assertThat(result.financeResult.code).isEqualTo(FinanceCode.LABELS_UPDATE_FAILURE.code)
        assertThat(remote.mergedUserFields).isEmpty()
    }

    @Test
    fun undoDeleteLabel_restoresLabelAndExpenses() = runBlocking<Unit> {
        val pizza = testExpense(name = "Pizza", id = "p", labels = listOf("Dinner", "Work"))
        onIo { expenseDao.upsert(pizza) }
        subject.deleteLabel("Work")
        remote.sets.clear()

        val result = subject.undoDeleteLabel("Work", listOf(pizza))

        assertThat(result.code).isEqualTo(FinanceCode.LABELS_UPDATE_SUCCESS.code)
        assertThat(prefs.labels).containsExactly("Dinner", "Work")
        assertThat(expenseDao.getById("p")!!.labels).containsExactly("Dinner", "Work")
        assertThat(remote.sets.map { it.second }).containsExactly("p")
    }

    @Test
    fun editLabel_renamesItEverywhere() = runBlocking<Unit> {
        onIo { expenseDao.upsert(testExpense(name = "Pizza", id = "p", labels = listOf("Dinner", "Work"))) }
        onIo { expenseDao.upsert(testExpense(name = "Milk", id = "m", labels = listOf("Dinner"))) }

        val result = subject.editLabel("Work", " Office ")

        assertThat(result.code).isEqualTo(FinanceCode.LABEL_UPDATE_SUCCESS.code)
        assertThat(prefs.labels).containsExactly("Dinner", "Office").inOrder()
        assertThat(expenseDao.getById("p")!!.labels).containsExactly("Dinner", "Office")
        assertThat(expenseDao.getById("m")!!.labels).containsExactly("Dinner")
        assertThat(remote.sets.map { it.second }).containsExactly("p")
    }

    @Test
    fun editLabel_blankOrUnknown_fails() = runBlocking {
        assertThat(subject.editLabel("Work", " ").code).isEqualTo(FinanceCode.LABELS_UPDATE_FAILURE.code)
        assertThat(subject.editLabel("Gift", "Present").code).isEqualTo(FinanceCode.LABELS_UPDATE_FAILURE.code)
        assertThat(remote.mergedUserFields).isEmpty()
    }

    @Test
    fun incomingExpense_unknownLabelsAreStripped() = runBlocking<Unit> {
        val initialSync = CompletableDeferred<Unit>()
        subject.startSnapshotListener(scope, initialSync)
        awaitUntil { remote.activeListeners == 1 }

        remote.sendChanges(collection, testExpense(name = "Pizza", id = "p", labels = listOf("Dinner", "Retired")).copy(updatedAt = 10L))

        initialSync.await()
        assertThat(expenseDao.getById("p")!!.labels).containsExactly("Dinner")
    }

    @Test
    fun incomingExpense_whenNoLabelsExistYet_keepsItsLabels() = runBlocking<Unit> {
        preferences.setLabels(emptyList())
        val initialSync = CompletableDeferred<Unit>()
        subject.startSnapshotListener(scope, initialSync)
        awaitUntil { remote.activeListeners == 1 }

        remote.sendChanges(collection, testExpense(name = "Pizza", id = "p", labels = listOf("Dinner")).copy(updatedAt = 10L))

        initialSync.await()
        assertThat(expenseDao.getById("p")!!.labels).containsExactly("Dinner")
    }

    // endregion

    // region root listener

    @Test
    fun startRootSnapshotListener_appliesRootFields_andSortsLabels() = runBlocking {
        val initialSync = CompletableDeferred<Unit>()
        subject.startRootSnapshotListener(scope, initialSync)
        awaitUntil { remote.activeListeners == 1 }

        remote.sendUserSnapshot(
            exists = true,
            data = mapOf(
                FirestoreEnums.FIELDS.MONTHLY_BUDGET.value to 300L,
                FirestoreEnums.FIELDS.CURRENCY_CODE.value to "GBP",
                FirestoreEnums.FIELDS.LABELS.value to listOf("Work", "Dinner", 42),
                FirestoreEnums.FIELDS.PRO_PIC_CHOICE.value to "pana",
            ),
        )

        initialSync.await()
        assertThat(prefs.monthlyBudget).isEqualTo(300.0)
        assertThat(prefs.currencyCode).isEqualTo("GBP")
        assertThat(prefs.labels).containsExactly("Dinner", "Work").inOrder()
        assertThat(prefs.proPicChoice).isEqualTo("pana")
    }

    @Test
    fun startRootSnapshotListener_missingFields_fallBackToDefaults() = runBlocking {
        preferences.setPreferences(testPreferences(user = testUser(), monthlyBudget = 900.0, currencyCode = "USD", labels = listOf("Old"), proPicChoice = "cuate"))
        val initialSync = CompletableDeferred<Unit>()
        subject.startRootSnapshotListener(scope, initialSync)
        awaitUntil { remote.activeListeners == 1 }

        remote.sendUserSnapshot(exists = true, data = emptyMap())

        initialSync.await()
        assertThat(prefs.monthlyBudget).isEqualTo(0.0)
        assertThat(prefs.currencyCode).isEqualTo("EUR")
        assertThat(prefs.labels).isEmpty()
        // A missing choice is not a reset: the local choice stays.
        assertThat(prefs.proPicChoice).isEqualTo("cuate")
    }

    @Test
    fun startRootSnapshotListener_missingDocument_completesDeferred_withoutChanges() = runBlocking<Unit> {
        val initialSync = CompletableDeferred<Unit>()
        subject.startRootSnapshotListener(scope, initialSync)
        awaitUntil { remote.activeListeners == 1 }

        remote.sendUserSnapshot(exists = false)

        initialSync.await()
        assertThat(prefs.labels).containsExactly("Dinner", "Work")
    }

    @Test
    fun startRootSnapshotListener_error_completesDeferred() = runBlocking {
        val initialSync = CompletableDeferred<Unit>()
        subject.startRootSnapshotListener(scope, initialSync)
        awaitUntil { remote.activeListeners == 1 }

        remote.sendUserListenerError(IOException("permission denied"))

        initialSync.await()
    }

    @Test
    fun startRootSnapshotListener_withoutUser_completesDeferred() = runBlocking {
        preferences.setUser(null)
        val initialSync = CompletableDeferred<Unit>()

        subject.startRootSnapshotListener(scope, initialSync)

        initialSync.await()
        assertThat(remote.activeListeners).isEqualTo(0)
    }

    @Test
    fun stopSnapshotListener_removesBothListeners() = runBlocking {
        subject.startSnapshotListener(scope)
        subject.startRootSnapshotListener(scope)
        awaitUntil { remote.activeListeners == 2 }

        subject.stopSnapshotListener()

        assertThat(remote.activeListeners).isEqualTo(0)
        assertThat(remote.removedListeners).isEqualTo(2)
    }

    // endregion
}
