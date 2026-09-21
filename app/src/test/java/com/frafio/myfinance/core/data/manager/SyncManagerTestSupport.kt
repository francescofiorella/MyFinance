package com.frafio.myfinance.core.data.manager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * The managers get a real background dispatcher so Room's main-thread guard stays armed (a manager
 * that forgot `withContext(ioDispatcher)` would fail here as it would on a phone). The test thread is
 * Robolectric's main thread, so the tests' own blocking DAO calls go through [onIo], and effects of
 * the coroutines a listener spawns are polled with [awaitUntil] rather than raced.
 */
suspend fun <T> onIo(block: () -> T): T = withContext(Dispatchers.IO) { block() }

suspend fun awaitUntil(timeoutMs: Long = 5_000, condition: suspend () -> Boolean) {
    withTimeout(timeoutMs) {
        while (!condition()) delay(10)
    }
}
