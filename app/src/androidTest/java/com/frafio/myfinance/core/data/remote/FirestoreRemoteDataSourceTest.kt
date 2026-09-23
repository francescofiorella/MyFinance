package com.frafio.myfinance.core.data.remote

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.model.Transaction
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.data.testIncome
import com.frafio.myfinance.testing.firebase.FirebaseEmulator
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.BeforeClass
import org.junit.Test
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

/**
 * The real adapter against the Firestore emulator: the wire format, the decoder, the array and merge
 * updates and the `updatedAt` listener that `TestRemoteDataSource` imitates. Each test writes under
 * its own random user, so nothing is shared between tests.
 */
class FirestoreRemoteDataSourceTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun connectToEmulator() {
            FirebaseEmulator.connect()
            FirebaseEmulator.clearFirestore()
        }
    }

    private val subject = FirestoreRemoteDataSource()
    private val email = FirebaseEmulator.randomEmail()
    private val payments = FirestoreEnums.FIELDS.PAYMENTS.value
    private val incomes = FirestoreEnums.FIELDS.INCOMES.value

    private fun expense(name: String = "Pizza", labels: List<String> = listOf("Dinner"), updatedAt: Long? = 1_000L): Expense =
        testExpense(name = name, price = 8.5, date = LocalDate.of(2024, 5, 29), labels = labels).copy(updatedAt = updatedAt)

    private suspend fun raw(path: String): DocumentSnapshot = FirebaseFirestore.getInstance().document(path).get().await()

    private suspend fun <E> Channel<E>.next(): E = withTimeout(10_000.milliseconds) { receive() }

    private fun <T : Transaction> listen(collection: String, since: Long, type: Class<T>): Pair<Channel<RemoteSnapshot<T>>, RemoteListener> {
        val events = Channel<RemoteSnapshot<T>>(Channel.UNLIMITED)
        val listener = subject.listenChanges(email, collection, since, type) { snapshot, error ->
            if (error != null) events.close(error) else events.trySend(snapshot!!)
        }
        return events to listener
    }

    // region writes

    @Test
    fun add_returnsTheGeneratedId_andStoresExactlyTheWireFields() = runBlocking {
        val item = expense()

        val id = subject.add(email, payments, item)

        val document = raw("purchases/$email/payments/$id")
        assertThat(document.data!!.keys).containsExactlyElementsIn(item.toFirestoreMap().keys)
        assertThat(document.getString("name")).isEqualTo("Pizza")
        assertThat(document.getDouble("price")).isEqualTo(8.5)
        assertThat(document.getLong("year")).isEqualTo(2024)
        assertThat(document.getLong("timestamp")).isEqualTo(item.timestamp)
        assertThat(document.get("labels")).isEqualTo(listOf("Dinner"))
        assertThat(document.getLong("updatedAt")).isEqualTo(1_000L)
        assertThat(document.get("isDeleted")).isNull()
    }

    @Test
    fun getAll_decodesWhatAddWrote_andReportsIdPathAndRawData() = runBlocking {
        val item = expense()
        val id = subject.add(email, payments, item)

        val document = subject.getAll(email, payments, Expense::class.java).single()

        assertThat(document.id).isEqualTo(id)
        assertThat(document.path).isEqualTo("purchases/$email/payments/$id")
        assertThat(document.data).containsEntry("name", "Pizza")
        // The id is @Exclude'd: the sync managers copy it from the document, as here.
        val decoded = document.decode()!!.apply { this.id = document.id }
        assertThat(decoded).isEqualTo(item.copy(id = id))
    }

    @Test
    fun getAll_decodesAnIncome() = runBlocking {
        val item = testIncome(name = "Salary", price = 2500.0).copy(updatedAt = 5L)
        val id = subject.add(email, incomes, item)

        val document = subject.getAll(email, incomes, Income::class.java).single()

        assertThat(document.path).isEqualTo("purchases/$email/incomes/$id")
        assertThat(document.decode()!!.apply { this.id = document.id }).isEqualTo(item.copy(id = id))
    }

    @Test
    fun set_writesAtTheGivenId_andOverwrites() = runBlocking {
        subject.set(email, payments, "fixed", expense(name = "Pizza"))
        subject.set(email, payments, "fixed", expense(name = "Pasta"))

        val document = subject.getAll(email, payments, Expense::class.java).single()

        assertThat(document.id).isEqualTo("fixed")
        assertThat(document.decode()!!.name).isEqualTo("Pasta")
    }

    @Test
    fun deletionFields_roundTrip_underTheIsDeletedName() = runBlocking {
        subject.set(email, payments, "gone", expense().copy(isDeleted = true, deleteAt = 99L))

        assertThat(raw("purchases/$email/payments/gone").getBoolean("isDeleted")).isTrue()
        val decoded = subject.getAll(email, payments, Expense::class.java).single().decode()!!
        assertThat(decoded.isDeleted).isTrue()
        assertThat(decoded.deleteAt).isEqualTo(99L)
    }

    @Test
    fun updateArrayField_add_isIdempotent_andStampsUpdatedAt() = runBlocking {
        subject.set(email, payments, "p", expense(labels = listOf("Dinner")))

        subject.updateArrayField(email, payments, "p", FirestoreEnums.FIELDS.LABELS.value, "Work", add = true, updatedAt = 2_000L)
        subject.updateArrayField(email, payments, "p", FirestoreEnums.FIELDS.LABELS.value, "Work", add = true, updatedAt = 3_000L)

        val document = raw("purchases/$email/payments/p")
        assertThat(document.get("labels")).isEqualTo(listOf("Dinner", "Work"))
        assertThat(document.getLong("updatedAt")).isEqualTo(3_000L)
    }

    @Test
    fun updateArrayField_remove_dropsTheValue_andStampsUpdatedAt() = runBlocking {
        subject.set(email, payments, "p", expense(labels = listOf("Dinner", "Work")))

        subject.updateArrayField(email, payments, "p", FirestoreEnums.FIELDS.LABELS.value, "Dinner", add = false, updatedAt = 2_000L)

        val document = raw("purchases/$email/payments/p")
        assertThat(document.get("labels")).isEqualTo(listOf("Work"))
        assertThat(document.getLong("updatedAt")).isEqualTo(2_000L)
    }

    @Test
    fun getAll_explicitNullField_isVisibleInTheRawData() = runBlocking {
        FirebaseFirestore.getInstance().document("purchases/$email/payments/n")
            .set(mapOf("name" to "Pizza", "price" to null)).await()

        val document = subject.getAll(email, payments, Expense::class.java).single()

        assertThat(document.data).containsEntry("price", null)
    }

    // endregion

    // region listeners

    @Test
    fun listenChanges_firstSnapshot_holdsOnlyNewerDocuments_oldestFirst() = runBlocking {
        subject.set(email, payments, "a", expense(name = "A", updatedAt = 100L))
        subject.set(email, payments, "b", expense(name = "B", updatedAt = 300L))
        subject.set(email, payments, "c", expense(name = "C", updatedAt = 200L))

        val (events, listener) = listen(payments, since = 150L, Expense::class.java)
        val first = events.next()
        listener.remove()

        assertThat(first.changes.map { it.id }).containsExactly("c", "b").inOrder()
        assertThat(first.isEmpty).isFalse()
    }

    @Test
    fun listenChanges_emptyCollection_reportsEmpty_thenALaterWriteArrives() = runBlocking {
        val (events, listener) = listen(payments, since = 0L, Expense::class.java)

        val first = events.next()
        subject.set(email, payments, "a", expense(updatedAt = 10L))
        val second = events.next()
        listener.remove()

        assertThat(first.isEmpty).isTrue()
        assertThat(first.changes).isEmpty()
        assertThat(second.changes.map { it.id }).containsExactly("a")
        assertThat(second.isEmpty).isFalse()
    }

    @Test
    fun listenChanges_remove_stopsTheEvents() = runBlocking {
        val (events, listener) = listen(payments, since = 0L, Expense::class.java)
        events.next()

        listener.remove()
        subject.set(email, payments, "a", expense(updatedAt = 10L))

        assertThat(withTimeoutOrNull(2_000.milliseconds) { events.receive() }).isNull()
    }

    // endregion

    // region user root

    @Test
    fun mergeUserFields_keepsTheOtherFields() = runBlocking {
        subject.mergeUserFields(email, mapOf("monthly_budget" to 500.0, "currencyCode" to "EUR"))

        subject.mergeUserFields(email, mapOf("currencyCode" to "USD"))

        assertThat(raw("purchases/$email").data).isEqualTo(mapOf("monthly_budget" to 500.0, "currencyCode" to "USD"))
    }

    @Test
    fun listenUser_reportsAbsence_thenTheMergedData() = runBlocking {
        val events = Channel<Pair<Boolean, Map<String, Any?>?>>(Channel.UNLIMITED)
        val listener = subject.listenUser(email) { exists, data, error ->
            if (error != null) events.close(error) else events.trySend(exists to data)
        }

        val first = events.next()
        subject.mergeUserFields(email, mapOf("currencyCode" to "USD"))
        val second = events.next()
        listener.remove()

        assertThat(first.first).isFalse()
        assertThat(second.first).isTrue()
        assertThat(second.second).containsEntry("currencyCode", "USD")
    }

    // endregion
}
