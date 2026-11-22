package com.frafio.myfinance.core.data.enums

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale

/** The codes' messages, resolved from strings.xml in both supported languages. */
@RunWith(RobolectricTestRunner::class)
class CodeMessagesTest {

    private val english = localized(Locale.ENGLISH)
    private val italian = localized(Locale.ITALIAN)

    @Test
    fun financeCodes_haveAMessageInBothLanguages() {
        FinanceCode.entries.forEach { entry ->
            assertThat(english.getString(entry.messageRes)).isNotEmpty()
            assertThat(italian.getString(entry.messageRes)).isNotEmpty()
        }
    }

    @Test
    fun financeMessages_keepTheirTexts() {
        assertThat(english.getString(FinanceCode.EXPENSE_ADD_SUCCESS.messageRes)).isEqualTo("Expense added")
        assertThat(italian.getString(FinanceCode.EXPENSE_ADD_SUCCESS.messageRes)).isEqualTo("Spesa aggiunta")
        assertThat(english.getString(FinanceCode.WRONG_NAME_TOTAL.messageRes)).isEqualTo("The name can't be 'Total'")
        assertThat(italian.getString(FinanceCode.WRONG_AMOUNT.messageRes)).isEqualTo("L'importo non può essere zero")
    }

    private fun localized(locale: Locale): Context {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val configuration = Configuration(context.resources.configuration).apply { setLocale(locale) }
        return context.createConfigurationContext(configuration)
    }
}
