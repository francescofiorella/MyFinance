package com.frafio.myfinance.core.data.converters

import com.google.common.truth.Truth.assertThat
import org.junit.Test

// Runs on the device because org.json is a stub on the JVM.
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun emptyList_roundTrips() {
        assertThat(converters.fromStringList(emptyList())).isEqualTo("[]")
        assertThat(converters.toStringList("[]")).isEmpty()
    }

    @Test
    fun simpleList_roundTrips() {
        val labels = listOf("work", "travel")

        assertThat(converters.toStringList(converters.fromStringList(labels)))
            .containsExactlyElementsIn(labels).inOrder()
    }

    @Test
    fun specialCharacters_roundTripUnchanged() {
        val labels = listOf("say \"hi\"", "back\\slash", "a,b", "[brackets]", "caffè", "🎉")

        assertThat(converters.toStringList(converters.fromStringList(labels)))
            .containsExactlyElementsIn(labels).inOrder()
    }

    @Test
    fun duplicates_arePreserved() {
        val labels = listOf("x", "x")

        assertThat(converters.toStringList(converters.fromStringList(labels)))
            .containsExactly("x", "x")
    }
}
