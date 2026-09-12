package com.frafio.myfinance.core.di

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val myFinanceDispatcher: MyFinanceDispatchers)

enum class MyFinanceDispatchers {
    Default,
    IO,
}
