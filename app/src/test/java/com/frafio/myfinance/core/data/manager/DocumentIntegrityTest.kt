package com.frafio.myfinance.core.data.manager

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DocumentIntegrityTest {

    private val path = "purchases/ada@example.com/payments/abc"

    private val complete: Map<String, Any?> = mapOf(
        "name" to "Coffee",
        "price" to 1.5,
        "year" to 2024,
        "month" to 6,
        "day" to 15,
        "timestamp" to 1_718_409_600_000L,
        "category" to 5,
        "labels" to listOf("cafe"),
    )

    @Test
    fun completeDocument_hasNoReport() {
        assertThat(DocumentIntegrity.check(path, complete)).isNull()
    }

    @Test
    fun missingField_isReportedAndStillLoadable() {
        val report = DocumentIntegrity.check(path, complete - "timestamp")

        assertThat(report).isNotNull()
        assertThat(report!!.path).isEqualTo(path)
        assertThat(report.missing).containsExactly("timestamp")
        assertThat(report.nulls).isEmpty()
        assertThat(report.isLoadable).isTrue()
    }

    @Test
    fun explicitNull_isReportedAndNotLoadable() {
        val report = DocumentIntegrity.check(path, complete + ("price" to null))

        assertThat(report!!.nulls).containsExactly("price")
        assertThat(report.missing).isEmpty()
        assertThat(report.isLoadable).isFalse()
    }

    @Test
    fun missingAndNull_areReportedSeparately() {
        val report = DocumentIntegrity.check(path, complete - "category" + ("name" to null))

        assertThat(report!!.missing).containsExactly("category")
        assertThat(report.nulls).containsExactly("name")
    }

    @Test
    fun optionalSyncFields_areNotRequired() {
        val withoutSyncFields = complete - "updatedAt" - "isDeleted" - "deleteAt"
        assertThat(DocumentIntegrity.check(path, withoutSyncFields)).isNull()
    }

    @Test
    fun tombstones_areNotChecked() {
        val tombstone = mapOf("isDeleted" to true, "updatedAt" to 1L)
        assertThat(DocumentIntegrity.check(path, tombstone)).isNull()
    }

    @Test
    fun noData_reportsEveryRequiredFieldMissing() {
        val report = DocumentIntegrity.check(path, null)

        assertThat(report!!.missing)
            .containsExactly("name", "price", "year", "month", "day", "timestamp", "category")
        assertThat(report.isLoadable).isTrue()
    }
}
