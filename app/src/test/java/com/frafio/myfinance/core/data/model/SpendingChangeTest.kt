package com.frafio.myfinance.core.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SpendingChangeTest {

    @Test
    fun spendingChange_coversEveryCase() {
        assertThat(spendingChange(current = 0.0, previous = 0.0)).isEqualTo(SpendingChange.None)
        assertThat(spendingChange(current = 10.0, previous = 0.0)).isEqualTo(SpendingChange.New)
        assertThat(spendingChange(current = 120.0, previous = 100.0)).isEqualTo(SpendingChange.Percent(0.2))
        assertThat(spendingChange(current = 0.0, previous = 50.0)).isEqualTo(SpendingChange.Percent(-1.0))
        assertThat(spendingChange(current = 50.0, previous = 50.0)).isEqualTo(SpendingChange.Percent(0.0))
    }
}
