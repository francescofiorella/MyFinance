package com.frafio.myfinance.core.data.model

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.data.testIncome
import com.frafio.myfinance.testing.remote.toRemoteMap
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.util.CustomClassMapper
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

/**
 * The mapping Firestore applies in `DocumentSnapshot.toObject` / `set(item)`, run through the same
 * `CustomClassMapper`. It is what the non-null defaults and `DocumentIntegrity` rely on.
 *
 * Plain JVM on purpose: under Robolectric the framework jar exposes the hidden
 * `Parcelable.getStability()`, which the mapper would serialise as a `stability` field; on a
 * device that member is hidden from app reflection, as it is in the SDK stubs used here.
 */
class FirestoreMappingTest {

    private val expense = testExpense(
        name = "Pizza", price = 8.5, date = LocalDate.of(2024, 5, 29), labels = listOf("Dinner", "Work"), id = "abc",
    ).copy(updatedAt = 1_000L, isDeleted = false, deleteAt = null)

    private fun serialize(item: Transaction): Map<String, Any?> {
        @Suppress("UNCHECKED_CAST")
        return CustomClassMapper.convertToPlainJavaTypes(item) as Map<String, Any?>
    }

    private fun <T : Transaction> deserialize(data: Map<String, Any?>, type: Class<T>): T =
        CustomClassMapper.convertToCustomClass(data, type, null)

    // Firestore returns every integer as Long; the fake keeps Kotlin's Int/Long.
    private fun Map<String, Any?>.asFirestoreWouldReturn(): Map<String, Any?> =
        mapValues { (_, v) -> if (v is Int) v.toLong() else v }

    @Test
    fun serialize_expense_producesTheWireFields() {
        val wire = serialize(expense)

        assertThat(wire).containsExactlyEntriesIn(expense.toRemoteMap())
        assertThat(wire.keys).doesNotContain("id")
        assertThat(wire.keys).doesNotContain("totalId")
        assertThat(wire.keys).contains(FirestoreEnums.FIELDS.IS_DELETED.value)
    }

    @Test
    fun serialize_income_producesTheWireFields() {
        val income = testIncome(id = "abc").copy(updatedAt = 5L, isDeleted = true, deleteAt = 9L)

        assertThat(serialize(income)).containsExactlyEntriesIn(income.toRemoteMap())
    }

    @Test
    fun deserialize_fullDocument_roundTrips() {
        val restored = deserialize(expense.toRemoteMap().asFirestoreWouldReturn(), Expense::class.java)
        restored.id = "abc"

        assertThat(restored).isEqualTo(expense)
    }

    @Test
    fun deserialize_kotlinTypedMap_roundTripsToo() {
        val restored = deserialize(expense.toRemoteMap(), Expense::class.java)
        restored.id = "abc"

        assertThat(restored).isEqualTo(expense)
    }

    @Test
    fun deserialize_missingFields_takeTheDefaults() {
        val partial = expense.toRemoteMap().asFirestoreWouldReturn() -
            setOf(FirestoreEnums.FIELDS.CATEGORY.value, FirestoreEnums.FIELDS.LABELS.value, FirestoreEnums.FIELDS.UPDATED_AT.value)

        val restored = deserialize(partial, Expense::class.java)

        assertThat(restored.name).isEqualTo("Pizza")
        assertThat(restored.category).isEqualTo(-1)
        assertThat(restored.labels).isEmpty()
        assertThat(restored.updatedAt).isNull()
    }

    @Test
    fun deserialize_explicitNull_inANonNullField_fails() {
        val broken = expense.toRemoteMap().asFirestoreWouldReturn() + (FirestoreEnums.FIELDS.PRICE.value to null)

        assertThrows(RuntimeException::class.java) { deserialize(broken, Expense::class.java) }
    }

    @Test
    fun deserialize_unknownKeys_areIgnored() {
        val extra = expense.toRemoteMap().asFirestoreWouldReturn() + mapOf("id" to "remote", "foo" to "bar")

        val restored = deserialize(extra, Expense::class.java)

        assertThat(restored.name).isEqualTo("Pizza")
        // `id` is @Exclude'd: the managers assign it from the document id afterwards.
        assertThat(restored.id).isNotEqualTo("remote")
    }

    @Test
    fun deserialize_tombstone() {
        val tomb = expense.copy(isDeleted = true, deleteAt = 2_000L).toRemoteMap().asFirestoreWouldReturn()

        val restored = deserialize(tomb, Expense::class.java)

        assertThat(restored.isDeleted).isTrue()
        assertThat(restored.deleteAt).isEqualTo(2_000L)
    }

    @Test
    fun income_roundTrips() {
        val income = testIncome(name = "Salary", price = 2500.0, id = "abc").copy(updatedAt = 7L, isDeleted = false)

        val restored = deserialize(income.toRemoteMap().asFirestoreWouldReturn(), Income::class.java)
        restored.id = "abc"

        assertThat(restored).isEqualTo(income)
    }

    @Test
    fun income_missingFields_takeTheDefaults() {
        val restored = deserialize(mapOf("name" to "Bonus"), Income::class.java)

        assertThat(restored.name).isEqualTo("Bonus")
        assertThat(restored.price).isEqualTo(0.0)
        assertThat(restored.year).isEqualTo(0)
        assertThat(restored.category).isEqualTo(-1)
        assertThat(restored.isDeleted).isNull()
    }
}
