package com.frafio.myfinance.core.data.manager

import android.util.Log
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums

/**
 * Reports transaction documents that would only load through the model's default values.
 * Watch the first sync with `adb logcat -s DataIntegrity`.
 */
object DocumentIntegrity {

    const val TAG = "DataIntegrity"

    private val requiredFields = listOf(
        FirestoreEnums.FIELDS.NAME,
        FirestoreEnums.FIELDS.PRICE,
        FirestoreEnums.FIELDS.YEAR,
        FirestoreEnums.FIELDS.MONTH,
        FirestoreEnums.FIELDS.DAY,
        FirestoreEnums.FIELDS.TIMESTAMP,
        FirestoreEnums.FIELDS.CATEGORY,
    ).map { it.value }

    data class Report(val path: String, val missing: List<String>, val nulls: List<String>) {
        // toObject() cannot write null into a non-null field; a missing field only takes the default.
        val isLoadable: Boolean get() = nulls.isEmpty()
    }

    fun check(path: String, data: Map<String, Any?>?): Report? {
        if (data == null) return Report(path, requiredFields, emptyList())
        if (data[FirestoreEnums.FIELDS.IS_DELETED.value] == true) return null
        val missing = requiredFields.filter { it !in data }
        val nulls = requiredFields.filter { it in data && data[it] == null }
        if (missing.isEmpty() && nulls.isEmpty()) return null
        return Report(path, missing, nulls)
    }

    /** Logs a malformed document and returns whether it is safe to deserialise. */
    fun verify(path: String, data: Map<String, Any?>?): Boolean {
        val report = check(path, data) ?: return true
        if (report.isLoadable) {
            Log.w(TAG, "${report.path}: missing ${report.missing} (defaults applied)")
        } else {
            Log.e(TAG, "${report.path}: null ${report.nulls}, missing ${report.missing} (skipped)")
        }
        return report.isLoadable
    }
}
