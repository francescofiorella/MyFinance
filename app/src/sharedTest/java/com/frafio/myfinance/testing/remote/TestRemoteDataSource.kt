package com.frafio.myfinance.testing.remote

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.model.Transaction
import com.frafio.myfinance.core.data.remote.RemoteDataSource
import com.frafio.myfinance.core.data.remote.RemoteDocument
import com.frafio.myfinance.core.data.remote.RemoteListener
import com.frafio.myfinance.core.data.remote.RemoteSnapshot

/**
 * In-memory stand-in for Firestore. Documents live in [collections] keyed by collection then id;
 * a test seeds them, drives the listeners with [sendChanges] / [sendUserSnapshot] and reads the
 * recorded calls. [failNext] makes the next remote call throw, as a network error would.
 */
class TestRemoteDataSource : RemoteDataSource {

    val collections: MutableMap<String, MutableMap<String, Transaction>> = mutableMapOf()
    val userFields: MutableMap<String, Any?> = mutableMapOf()

    var failNext: Throwable? = null

    /** Applied to every document handed to the manager; a test can strip or null a field. */
    var documentData: (Transaction) -> Map<String, Any?>? = { it.toRemoteMap() }

    val added = mutableListOf<Pair<String, Transaction>>()
    val sets = mutableListOf<Triple<String, String, Transaction>>()
    val arrayUpdates = mutableListOf<ArrayUpdate>()
    val mergedUserFields = mutableListOf<Map<String, Any>>()
    val getAllCalls = mutableListOf<String>()
    val listenSince = mutableMapOf<String, Long>()
    var removedListeners = 0
        private set

    private var nextId = 1
    private val changeListeners = mutableMapOf<String, (RemoteSnapshot<*>?, Throwable?) -> Unit>()
    private var userListener: ((Boolean, Map<String, Any?>?, Throwable?) -> Unit)? = null

    val activeListeners: Int get() = changeListeners.size + (if (userListener != null) 1 else 0)

    data class ArrayUpdate(val collection: String, val id: String, val field: String, val value: Any, val add: Boolean, val updatedAt: Long)

    fun seed(collection: String, vararg items: Transaction) {
        val store = collections.getOrPut(collection) { mutableMapOf() }
        items.forEach { store[it.id] = it }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Transaction> sendChanges(collection: String, vararg items: T) {
        val listener = checkNotNull(changeListeners[collection]) { "no listener on $collection" }
        val documents = items.map { it.toRemoteDocument(collection) as RemoteDocument<T> }
        listener(RemoteSnapshot(documents, isEmpty = documents.isEmpty()), null)
    }

    fun sendListenerError(collection: String, error: Throwable) {
        checkNotNull(changeListeners[collection]) { "no listener on $collection" }(null, error)
    }

    fun sendUserSnapshot(exists: Boolean, data: Map<String, Any?>? = if (exists) userFields.toMap() else null) {
        checkNotNull(userListener) { "no user listener" }(exists, data, null)
    }

    fun sendUserListenerError(error: Throwable) {
        checkNotNull(userListener) { "no user listener" }(false, null, error)
    }

    private fun throwIfFailing() {
        failNext?.let { failNext = null; throw it }
    }

    override suspend fun <T : Transaction> add(email: String, collection: String, item: T): String {
        throwIfFailing()
        val id = "remote-${nextId++}"
        added += collection to item
        seed(collection, item.withId(id))
        return id
    }

    override suspend fun <T : Transaction> set(email: String, collection: String, id: String, item: T) {
        throwIfFailing()
        sets += Triple(collection, id, item)
        seed(collection, item.withId(id))
    }

    override suspend fun updateArrayField(
        email: String,
        collection: String,
        id: String,
        field: String,
        value: Any,
        add: Boolean,
        updatedAt: Long,
    ) {
        throwIfFailing()
        arrayUpdates += ArrayUpdate(collection, id, field, value, add, updatedAt)
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Transaction> getAll(email: String, collection: String, type: Class<T>): List<RemoteDocument<T>> {
        throwIfFailing()
        getAllCalls += collection
        return collections[collection]?.values.orEmpty().map { it.toRemoteDocument(collection) as RemoteDocument<T> }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Transaction> listenChanges(
        email: String,
        collection: String,
        sinceUpdatedAt: Long,
        type: Class<T>,
        onEvent: (snapshot: RemoteSnapshot<T>?, error: Throwable?) -> Unit,
    ): RemoteListener {
        listenSince[collection] = sinceUpdatedAt
        changeListeners[collection] = onEvent as (RemoteSnapshot<*>?, Throwable?) -> Unit
        return RemoteListener {
            changeListeners.remove(collection)
            removedListeners++
        }
    }

    override suspend fun mergeUserFields(email: String, fields: Map<String, Any>) {
        throwIfFailing()
        mergedUserFields += fields
        userFields.putAll(fields)
    }

    override fun listenUser(
        email: String,
        onEvent: (exists: Boolean, data: Map<String, Any?>?, error: Throwable?) -> Unit,
    ): RemoteListener {
        userListener = onEvent
        return RemoteListener {
            userListener = null
            removedListeners++
        }
    }

    private fun Transaction.toRemoteDocument(collection: String): RemoteDocument<Transaction> =
        RemoteDocument(id = id, path = "purchases/user/$collection/$id", data = documentData(this)) { this }
}

/** The fields Firestore would hold for a transaction, keyed as `DocumentIntegrity` expects. */
fun Transaction.toRemoteMap(): Map<String, Any?> = mapOf(
    FirestoreEnums.FIELDS.NAME.value to name,
    FirestoreEnums.FIELDS.PRICE.value to price,
    FirestoreEnums.FIELDS.YEAR.value to year,
    FirestoreEnums.FIELDS.MONTH.value to month,
    FirestoreEnums.FIELDS.DAY.value to day,
    FirestoreEnums.FIELDS.TIMESTAMP.value to timestamp,
    FirestoreEnums.FIELDS.CATEGORY.value to category,
    FirestoreEnums.FIELDS.LABELS.value to labels,
    FirestoreEnums.FIELDS.UPDATED_AT.value to updatedAt,
    FirestoreEnums.FIELDS.IS_DELETED.value to isDeleted,
    FirestoreEnums.FIELDS.DELETE_AT.value to deleteAt,
)

private fun Transaction.withId(newId: String): Transaction = when (this) {
    is Expense -> copy(id = newId)
    is Income -> copy(id = newId)
}
