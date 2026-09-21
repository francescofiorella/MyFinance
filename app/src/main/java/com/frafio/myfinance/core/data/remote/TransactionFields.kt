package com.frafio.myfinance.core.data.remote

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Transaction

/**
 * The document written for a transaction. Explicit rather than reflective so the wire format is
 * exactly these fields: `id` is the document id, and synthetic members Kotlin, Compose or the
 * platform add to the class never leak into Firestore.
 */
fun Transaction.toFirestoreMap(): Map<String, Any?> = mapOf(
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
