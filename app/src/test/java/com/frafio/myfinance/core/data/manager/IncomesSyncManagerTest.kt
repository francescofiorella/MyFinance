package com.frafio.myfinance.core.data.manager

import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.utils.currentTimestampUTC
import com.frafio.myfinance.testing.data.testIncome
import com.frafio.myfinance.testing.data.testPreferences
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.remote.TestRemoteDataSource
import com.frafio.myfinance.testing.remote.toRemoteMap
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

/** The behaviour of `BaseSyncManager`, exercised through its simpler subclass over a real in-memory database. */
@RunWith(RobolectricTestRunner::class)
class IncomesSyncManagerTest : DatabaseTest() {

    private val collection = FirestoreEnums.FIELDS.INCOMES.value
    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private lateinit var preferences: TestUserPreferencesRepository
    private lateinit var remote: TestRemoteDataSource
    private lateinit var subject: IncomesSyncManager

    @Before
    fun setup() {
        preferences = TestUserPreferencesRepository().apply { setPreferences(testPreferences(user = testUser())) }
        remote = TestRemoteDataSource()
        subject = IncomesSyncManager(preferences, db, incomeDao, remote, dispatcher)
    }

    @After
    fun teardown() {
        scope.cancel()
    }

    // region add / edit / delete

    @Test
    fun add_storesRemoteIdAndUpsertsLocally() = runBlocking<Unit> {
        val before = currentTimestampUTC()

        val result = subject.add(testIncome(name = "Salary", id = "local"))

        assertThat(result.code).isEqualTo(FinanceCode.INCOME_ADD_SUCCESS.code)
        val (remoteCollection, sent) = remote.added.single()
        assertThat(remoteCollection).isEqualTo(collection)
        assertThat(sent.isDeleted).isFalse()
        assertThat(sent.updatedAt).isAtLeast(before)
        val local = onIo { incomeDao.getAllSync() }.single()
        assertThat(local.id).isEqualTo("remote-1")
        assertThat(local.name).isEqualTo("Salary")
    }

    @Test
    fun add_withoutUser_failsWithoutTouchingRemote() = runBlocking<Unit> {
        preferences.setUser(null)

        val result = subject.add(testIncome())

        assertThat(result.code).isEqualTo(FinanceCode.INCOME_ADD_FAILURE.code)
        assertThat(remote.added).isEmpty()
        assertThat(onIo { incomeDao.getAllSync() }).isEmpty()
    }

    @Test
    fun add_remoteFailure_returnsFailureCodeAndLeavesLocalUntouched() = runBlocking<Unit> {
        remote.failNext = IOException("offline")

        val result = subject.add(testIncome())

        assertThat(result.code).isEqualTo(FinanceCode.INCOME_ADD_FAILURE.code)
        assertThat(onIo { incomeDao.getAllSync() }).isEmpty()
    }

    @Test
    fun edit_setsRemoteAndUpsertsLocally_withNewUpdatedAt() = runBlocking<Unit> {
        val stale = testIncome(name = "Old", id = "abc").copy(updatedAt = 1L)
        onIo { incomeDao.upsert(stale) }

        val result = subject.edit(stale.copy(name = "New"))

        assertThat(result.code).isEqualTo(FinanceCode.INCOME_EDIT_SUCCESS.code)
        val (_, id, sent) = remote.sets.single()
        assertThat(id).isEqualTo("abc")
        assertThat(sent.name).isEqualTo("New")
        assertThat(sent.updatedAt).isGreaterThan(1L)
        assertThat(onIo { incomeDao.getAllSync() }.single().name).isEqualTo("New")
    }

    @Test
    fun edit_remoteFailure_keepsTheLocalRow() = runBlocking<Unit> {
        val stored = testIncome(name = "Old", id = "abc")
        onIo { incomeDao.upsert(stored) }
        remote.failNext = IOException("offline")

        val result = subject.edit(stored.copy(name = "New"))

        assertThat(result.code).isEqualTo(FinanceCode.INCOME_EDIT_FAILURE.code)
        assertThat(onIo { incomeDao.getAllSync() }.single().name).isEqualTo("Old")
    }

    @Test
    fun delete_softDeletesRemotely_andRemovesLocally() = runBlocking<Unit> {
        val stored = testIncome(id = "abc")
        onIo { incomeDao.upsert(stored) }
        val before = currentTimestampUTC()

        val result = subject.delete(stored)

        assertThat(result.code).isEqualTo(FinanceCode.INCOME_DELETE_SUCCESS.code)
        val (_, id, sent) = remote.sets.single()
        assertThat(id).isEqualTo("abc")
        assertThat(sent.isDeleted).isTrue()
        assertThat(sent.updatedAt).isAtLeast(before)
        // Tombstones are purged 30 days later.
        assertThat(sent.deleteAt).isAtLeast(before + 29L * 24 * 60 * 60 * 1000)
        assertThat(onIo { incomeDao.getAllSync() }).isEmpty()
    }

    @Test
    fun delete_remoteFailure_keepsTheLocalRow() = runBlocking<Unit> {
        val stored = testIncome(id = "abc")
        onIo { incomeDao.upsert(stored) }
        remote.failNext = IOException("offline")

        val result = subject.delete(stored)

        assertThat(result.code).isEqualTo(FinanceCode.INCOME_DELETE_FAILURE.code)
        assertThat(onIo { incomeDao.getAllSync() }).hasSize(1)
    }

    // endregion

    // region snapshot listener

    @Test
    fun startSnapshotListener_withoutUser_completesDeferredAndListensToNothing() = runBlocking<Unit> {
        preferences.setUser(null)
        val initialSync = CompletableDeferred<Unit>()

        subject.startSnapshotListener(scope, initialSync)

        initialSync.await()
        assertThat(remote.activeListeners).isEqualTo(0)
    }

    @Test
    fun startSnapshotListener_listensSinceLastSync() = runBlocking<Unit> {
        preferences.setPreferences(testPreferences(user = testUser()).copy(lastIncomesSync = 4_000L))

        subject.startSnapshotListener(scope)

        awaitUntil { remote.activeListeners == 1 }
        assertThat(remote.listenSince[collection]).isEqualTo(4_000L)
    }

    @Test
    fun startSnapshotListener_firstEmptySnapshot_completesDeferred() = runBlocking<Unit> {
        val initialSync = CompletableDeferred<Unit>()
        subject.startSnapshotListener(scope, initialSync)
        awaitUntil { remote.activeListeners == 1 }

        remote.sendChanges<Income>(collection)

        initialSync.await()
        assertThat(preferences.userPreferencesFlow.value.lastIncomesSync).isEqualTo(0L)
    }

    @Test
    fun startSnapshotListener_changes_upsertsAndDeletesLocally_andAdvancesLastSync() = runBlocking<Unit> {
        onIo { incomeDao.upsert(testIncome(name = "Gone", id = "gone")) }
        val initialSync = CompletableDeferred<Unit>()
        subject.startSnapshotListener(scope, initialSync)
        awaitUntil { remote.activeListeners == 1 }

        remote.sendChanges(
            collection,
            testIncome(name = "Fresh", id = "fresh").copy(updatedAt = 500L),
            testIncome(name = "Gone", id = "gone").copy(updatedAt = 700L, isDeleted = true),
        )

        initialSync.await()
        assertThat(onIo { incomeDao.getAllSync() }.map { it.name }).containsExactly("Fresh")
        assertThat(preferences.userPreferencesFlow.value.lastIncomesSync).isEqualTo(700L)
        assertThat(preferences.userPreferencesFlow.value.lastIncomesAppSync).isGreaterThan(0L)
    }

    @Test
    fun startSnapshotListener_laterChanges_keepApplying() = runBlocking<Unit> {
        val initialSync = CompletableDeferred<Unit>()
        subject.startSnapshotListener(scope, initialSync)
        awaitUntil { remote.activeListeners == 1 }
        remote.sendChanges(collection, testIncome(name = "First", id = "1").copy(updatedAt = 100L))
        initialSync.await()

        remote.sendChanges(collection, testIncome(name = "Second", id = "2").copy(updatedAt = 200L))

        awaitUntil { onIo { incomeDao.getAllSync() }.size == 2 }
        assertThat(preferences.userPreferencesFlow.value.lastIncomesSync).isEqualTo(200L)
    }

    @Test
    fun startSnapshotListener_documentWithNulls_isSkipped() = runBlocking<Unit> {
        remote.documentData = { item -> item.toRemoteMap() + (FirestoreEnums.FIELDS.PRICE.value to null) }
        val initialSync = CompletableDeferred<Unit>()
        subject.startSnapshotListener(scope, initialSync)
        awaitUntil { remote.activeListeners == 1 }

        remote.sendChanges(collection, testIncome(name = "Broken", id = "broken").copy(updatedAt = 900L))

        initialSync.await()
        assertThat(onIo { incomeDao.getAllSync() }).isEmpty()
        assertThat(preferences.userPreferencesFlow.value.lastIncomesSync).isEqualTo(0L)
    }

    @Test
    fun startSnapshotListener_documentMissingAField_isLoadedWithDefaults() = runBlocking<Unit> {
        remote.documentData = { item -> item.toRemoteMap() - FirestoreEnums.FIELDS.CATEGORY.value }
        val initialSync = CompletableDeferred<Unit>()
        subject.startSnapshotListener(scope, initialSync)
        awaitUntil { remote.activeListeners == 1 }

        remote.sendChanges(collection, testIncome(name = "Partial", id = "partial").copy(updatedAt = 900L))

        initialSync.await()
        assertThat(onIo { incomeDao.getAllSync() }.map { it.name }).containsExactly("Partial")
    }

    @Test
    fun startSnapshotListener_error_completesDeferred() = runBlocking<Unit> {
        val initialSync = CompletableDeferred<Unit>()
        subject.startSnapshotListener(scope, initialSync)
        awaitUntil { remote.activeListeners == 1 }

        remote.sendListenerError(collection, IOException("permission denied"))

        initialSync.await()
        assertThat(onIo { incomeDao.getAllSync() }).isEmpty()
    }

    @Test
    fun startSnapshotListener_twice_isNoOp() = runBlocking<Unit> {
        subject.startSnapshotListener(scope)
        awaitUntil { remote.activeListeners == 1 }
        val second = CompletableDeferred<Unit>()

        subject.startSnapshotListener(scope, second)

        second.await()
        assertThat(remote.activeListeners).isEqualTo(1)
    }

    @Test
    fun stopSnapshotListener_removesRegistration() = runBlocking<Unit> {
        subject.startSnapshotListener(scope)
        awaitUntil { remote.activeListeners == 1 }

        subject.stopSnapshotListener()

        assertThat(remote.activeListeners).isEqualTo(0)
        assertThat(remote.removedListeners).isEqualTo(1)
    }

    // endregion

    // region full sync

    private val thirtyDaysAgo get() = currentTimestampUTC() - 30L * 24 * 60 * 60 * 1000

    @Test
    fun fullSync_runsWhenAppSyncIsOlderThanThreshold() = runBlocking<Unit> {
        preferences.setPreferences(testPreferences(user = testUser()).copy(lastIncomesSync = 100L, lastIncomesAppSync = thirtyDaysAgo))
        onIo { incomeDao.upsert(testIncome(name = "Local only", id = "local")) }
        remote.seed(
            collection,
            testIncome(name = "Remote", id = "remote").copy(updatedAt = 5_000L),
            testIncome(name = "Tombstone", id = "tomb").copy(updatedAt = 6_000L, isDeleted = true),
        )
        val initialSync = CompletableDeferred<Unit>()

        subject.startSnapshotListener(scope, initialSync)

        awaitUntil { remote.activeListeners == 1 }
        assertThat(remote.getAllCalls).containsExactly(collection)
        assertThat(onIo { incomeDao.getAllSync() }.map { it.name }).containsExactly("Remote")
        assertThat(preferences.userPreferencesFlow.value.lastIncomesSync).isEqualTo(6_000L)
        assertThat(preferences.userPreferencesFlow.value.lastIncomesAppSync).isGreaterThan(thirtyDaysAgo)
        // The listener then continues from the synced timestamp.
        assertThat(remote.listenSince[collection]).isEqualTo(6_000L)
    }

    @Test
    fun fullSync_isSkipped_whenAppSyncIsRecent() = runBlocking<Unit> {
        preferences.setPreferences(testPreferences(user = testUser()).copy(lastIncomesSync = 100L, lastIncomesAppSync = currentTimestampUTC()))
        remote.seed(collection, testIncome(name = "Remote", id = "remote"))

        subject.startSnapshotListener(scope)

        awaitUntil { remote.activeListeners == 1 }
        assertThat(remote.getAllCalls).isEmpty()
        assertThat(onIo { incomeDao.getAllSync() }).isEmpty()
        assertThat(remote.listenSince[collection]).isEqualTo(100L)
    }

    @Test
    fun fullSync_isSkipped_onFirstEverStart() = runBlocking<Unit> {
        preferences.setPreferences(testPreferences(user = testUser()).copy(lastIncomesAppSync = 0L))
        remote.seed(collection, testIncome(name = "Remote", id = "remote"))

        subject.startSnapshotListener(scope)

        awaitUntil { remote.activeListeners == 1 }
        assertThat(remote.getAllCalls).isEmpty()
    }

    @Test
    fun fullSync_remoteFailure_keepsListeningFromTheOldTimestamp() = runBlocking<Unit> {
        preferences.setPreferences(testPreferences(user = testUser()).copy(lastIncomesSync = 100L, lastIncomesAppSync = thirtyDaysAgo))
        onIo { incomeDao.upsert(testIncome(name = "Local", id = "local")) }
        remote.failNext = IOException("offline")

        subject.startSnapshotListener(scope)

        awaitUntil { remote.activeListeners == 1 }
        assertThat(onIo { incomeDao.getAllSync() }).hasSize(1)
        assertThat(remote.listenSince[collection]).isEqualTo(100L)
    }

    // endregion
}
