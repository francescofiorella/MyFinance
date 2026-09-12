package com.frafio.myfinance.core.data.enums

import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.DeleteLabelResult
import com.frafio.myfinance.core.data.model.FinanceResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FinanceCodeTest {

    @Test
    fun codesTheViewModelsBranchOn_areStable() {
        assertThat(FinanceCode.EXPENSE_ADD_SUCCESS.code).isEqualTo(10)
        assertThat(FinanceCode.EXPENSE_EDIT_SUCCESS.code).isEqualTo(12)
        assertThat(FinanceCode.EXPENSE_EDIT_FAILURE.code).isEqualTo(13)
        assertThat(FinanceCode.EXPENSE_DELETE_SUCCESS.code).isEqualTo(14)
        assertThat(FinanceCode.INCOME_ADD_SUCCESS.code).isEqualTo(22)
        assertThat(FinanceCode.INCOME_DELETE_SUCCESS.code).isEqualTo(24)
        assertThat(FinanceCode.INCOME_EDIT_SUCCESS.code).isEqualTo(26)
        assertThat(FinanceCode.BUDGET_UPDATE_SUCCESS.code).isEqualTo(30)
        assertThat(FinanceCode.LABELS_UPDATE_SUCCESS.code).isEqualTo(40)
        assertThat(FinanceCode.LABEL_DELETE_SUCCESS.code).isEqualTo(42)
        assertThat(FinanceCode.LABEL_UPDATE_SUCCESS.code).isEqualTo(43)
        assertThat(FinanceCode.LABEL_ADD_SUCCESS.code).isEqualTo(44)
    }

    @Test
    fun codes_areUnique() {
        val duplicated = FinanceCode.entries.groupBy { it.code }.filterValues { it.size > 1 }.keys
        assertThat(duplicated).isEmpty()
    }

    @Test
    fun everyEntry_hasAMessage() {
        FinanceCode.entries.forEach { entry ->
            assertThat(entry.message).isNotEmpty()
        }
    }

    @Test
    fun messages_areEnglishUnderTheTestJvmLocale() {
        assertThat(FinanceCode.EXPENSE_ADD_SUCCESS.message).isEqualTo("Expense added")
        assertThat(FinanceCode.LABEL_DELETE_SUCCESS.message).isEqualTo("Label deleted")
    }

    @Test
    fun financeResult_exposesCodeAndMessage() {
        val result = FinanceResult(FinanceCode.EXPENSE_ADD_SUCCESS)
        assertThat(result.code).isEqualTo(10)
        assertThat(result.message).isEqualTo("Expense added")
    }

    @Test
    fun financeResult_customMessageOverridesTheEnumMessage() {
        val result = FinanceResult(FinanceCode.EXPENSE_ADD_FAILURE, "Custom")
        assertThat(result.message).isEqualTo("Custom")
    }

    @Test
    fun deleteLabelResult_defaultsToNoAffectedExpenses() {
        val result = DeleteLabelResult(FinanceResult(FinanceCode.LABEL_DELETE_SUCCESS))
        assertThat(result.affectedExpenses).isEmpty()
    }

    @Test
    fun categories_haveStableValues() {
        assertThat(FirestoreEnums.CATEGORIES.HOUSING.value).isEqualTo(0)
        assertThat(FirestoreEnums.CATEGORIES.MISCELLANEOUS.value).isEqualTo(8)
        assertThat(FirestoreEnums.CATEGORIES.TOTAL.value).isEqualTo(100)
        assertThat(FirestoreEnums.CATEGORIES.INCOME.value).isEqualTo(101)
        assertThat(FirestoreEnums.CATEGORIES.JOLLY.value).isEqualTo(102)
    }

    @Test
    fun spendingCategories_areTheNineBelowTotal() {
        val spending = FirestoreEnums.CATEGORIES.entries.filter { it.value < 100 }
        assertThat(spending).hasSize(9)
        assertThat(spending.map { it.value }).containsExactlyElementsIn(0..8).inOrder()
    }

    @Test
    fun totalName_isEnglishUnderTheTestJvmLocale() {
        assertThat(FirestoreEnums.NAMES.TOTAL.value).isEqualTo("Total")
        assertThat(FirestoreEnums.NAMES.TOTAL.valueEn).isEqualTo("Total")
        assertThat(FirestoreEnums.NAMES.TOTAL.valueIt).isEqualTo("Totale")
    }
}
