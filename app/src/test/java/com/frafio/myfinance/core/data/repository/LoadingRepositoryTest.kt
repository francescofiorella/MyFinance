package com.frafio.myfinance.core.data.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LoadingRepositoryTest {

    private lateinit var subject: LoadingRepository

    @Before
    fun setup() {
        subject = LoadingRepository()
    }

    @Test
    fun initially_notLoadingAndFirstSync() {
        assertThat(subject.isLoading.value).isFalse()
        assertThat(subject.isFirstSync.value).isTrue()
    }

    @Test
    fun startLoading_thenStopLoading_togglesIsLoading() {
        subject.startLoading()
        assertThat(subject.isLoading.value).isTrue()

        subject.stopLoading()
        assertThat(subject.isLoading.value).isFalse()
    }

    @Test
    fun nestedStarts_needMatchingStops() {
        subject.startLoading()
        subject.startLoading()

        subject.stopLoading()
        assertThat(subject.isLoading.value).isTrue()

        subject.stopLoading()
        assertThat(subject.isLoading.value).isFalse()
    }

    @Test
    fun stopLoading_belowZero_doesNotCorruptTheCounter() {
        subject.stopLoading()
        assertThat(subject.isLoading.value).isFalse()

        // A negative counter would need two starts to become loading again.
        subject.startLoading()
        assertThat(subject.isLoading.value).isTrue()
    }

    @Test
    fun manyStartsAndStops_endNotLoading() {
        repeat(3) { subject.startLoading() }
        repeat(3) { subject.stopLoading() }
        assertThat(subject.isLoading.value).isFalse()
    }

    @Test
    fun firstSync_togglesIndependentlyOfLoading() {
        subject.stopFirstSync()
        assertThat(subject.isFirstSync.value).isFalse()
        assertThat(subject.isLoading.value).isFalse()

        subject.startFirstSync()
        assertThat(subject.isFirstSync.value).isTrue()
    }

    @Test
    fun isLoading_conflatesRepeatedTrueAcrossNestedStarts() = runTest {
        val emissions = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { subject.isLoading.toList(emissions) }

        subject.startLoading()
        subject.startLoading()
        subject.stopLoading()
        subject.stopLoading()

        assertThat(emissions).containsExactly(false, true, false).inOrder()
    }
}
