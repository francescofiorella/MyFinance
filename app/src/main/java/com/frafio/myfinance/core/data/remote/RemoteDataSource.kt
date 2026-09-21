package com.frafio.myfinance.core.data.remote

import com.frafio.myfinance.core.data.model.Transaction

/** One remote document: its raw fields for the integrity check and a decoder for the typed model. */
class RemoteDocument<T : Transaction>(
    val id: String,
    val path: String,
    val data: Map<String, Any?>?,
    private val decoder: () -> T?,
) {
    fun decode(): T? = decoder()
}

/** A listener event: the documents that changed, plus whether the whole query result is empty. */
class RemoteSnapshot<T : Transaction>(
    val changes: List<RemoteDocument<T>>,
    val isEmpty: Boolean,
)

fun interface RemoteListener {
    fun remove()
}

/**
 * Everything the sync managers need from the remote store, in plain types. The only implementation
 * that knows Firebase is [FirestoreRemoteDataSource]; tests use `TestRemoteDataSource`.
 */
interface RemoteDataSource {

    /** Creates a document and returns its generated id. */
    suspend fun <T : Transaction> add(email: String, collection: String, item: T): String

    suspend fun <T : Transaction> set(email: String, collection: String, id: String, item: T)

    /** Adds or removes [value] from the array field [field] and stamps [updatedAt]. */
    suspend fun updateArrayField(
        email: String,
        collection: String,
        id: String,
        field: String,
        value: Any,
        add: Boolean,
        updatedAt: Long,
    )

    suspend fun <T : Transaction> getAll(email: String, collection: String, type: Class<T>): List<RemoteDocument<T>>

    /** Listens to documents whose `updatedAt` is greater than [sinceUpdatedAt], oldest first. */
    fun <T : Transaction> listenChanges(
        email: String,
        collection: String,
        sinceUpdatedAt: Long,
        type: Class<T>,
        onEvent: (snapshot: RemoteSnapshot<T>?, error: Throwable?) -> Unit,
    ): RemoteListener

    /** Merges [fields] into the user's root document. */
    suspend fun mergeUserFields(email: String, fields: Map<String, Any>)

    fun listenUser(
        email: String,
        onEvent: (exists: Boolean, data: Map<String, Any?>?, error: Throwable?) -> Unit,
    ): RemoteListener
}
