package com.frafio.myfinance.core.data.manager

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** `verify` logs through `android.util.Log`, hence Robolectric; the pure `check` is covered next door. */
@RunWith(RobolectricTestRunner::class)
class DocumentIntegrityVerifyTest {

    private val complete = mapOf(
        "name" to "Coffee", "price" to 1.5, "year" to 2024, "month" to 1, "day" to 15,
        "timestamp" to 1L, "category" to 2,
    )

    @Test
    fun verify_completeDocument_isLoadable() {
        assertThat(DocumentIntegrity.verify("purchases/a/payments/1", complete)).isTrue()
    }

    @Test
    fun verify_missingField_isLoadableWithDefaults() {
        assertThat(DocumentIntegrity.verify("purchases/a/payments/1", complete - "category")).isTrue()
    }

    @Test
    fun verify_nullField_isNotLoadable() {
        assertThat(DocumentIntegrity.verify("purchases/a/payments/1", complete + ("price" to null))).isFalse()
    }

    @Test
    fun verify_noData_isLoadable() {
        assertThat(DocumentIntegrity.verify("purchases/a/payments/1", null)).isTrue()
    }
}
