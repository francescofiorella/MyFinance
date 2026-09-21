package com.frafio.myfinance.core.data.remote

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Transaction
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRemoteDataSource @Inject constructor() : RemoteDataSource {

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private fun userDocument(email: String): DocumentReference =
        fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value).document(email)

    private fun collection(email: String, collection: String): CollectionReference =
        userDocument(email).collection(collection)

    private fun <T : Transaction> DocumentSnapshot.toRemoteDocument(type: Class<T>): RemoteDocument<T> =
        RemoteDocument(id = id, path = reference.path, data = data) { toObject(type) }

    override suspend fun <T : Transaction> add(email: String, collection: String, item: T): String =
        collection(email, collection).add(item.toFirestoreMap()).await().id

    override suspend fun <T : Transaction> set(email: String, collection: String, id: String, item: T) {
        collection(email, collection).document(id).set(item.toFirestoreMap()).await()
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
        val operation = if (add) FieldValue.arrayUnion(value) else FieldValue.arrayRemove(value)
        collection(email, collection).document(id)
            .update(field, operation, FirestoreEnums.FIELDS.UPDATED_AT.value, updatedAt)
            .await()
    }

    override suspend fun <T : Transaction> getAll(email: String, collection: String, type: Class<T>): List<RemoteDocument<T>> =
        collection(email, collection).get().await().documents.map { it.toRemoteDocument(type) }

    override fun <T : Transaction> listenChanges(
        email: String,
        collection: String,
        sinceUpdatedAt: Long,
        type: Class<T>,
        onEvent: (snapshot: RemoteSnapshot<T>?, error: Throwable?) -> Unit,
    ): RemoteListener {
        val registration = collection(email, collection)
            .whereGreaterThan(FirestoreEnums.FIELDS.UPDATED_AT.value, sinceUpdatedAt)
            .orderBy(FirestoreEnums.FIELDS.UPDATED_AT.value, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                val snapshot = snapshots?.let { query ->
                    RemoteSnapshot(
                        changes = query.documentChanges.map { it.document.toRemoteDocument(type) },
                        isEmpty = query.isEmpty,
                    )
                }
                onEvent(snapshot, error)
            }
        return RemoteListener { registration.remove() }
    }

    override suspend fun mergeUserFields(email: String, fields: Map<String, Any>) {
        userDocument(email).set(fields, SetOptions.merge()).await()
    }

    override fun listenUser(
        email: String,
        onEvent: (exists: Boolean, data: Map<String, Any?>?, error: Throwable?) -> Unit,
    ): RemoteListener {
        val registration = userDocument(email).addSnapshotListener { snapshot, error ->
            onEvent(snapshot != null && snapshot.exists(), snapshot?.data, error)
        }
        return RemoteListener { registration.remove() }
    }
}
