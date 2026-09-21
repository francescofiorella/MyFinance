package com.frafio.myfinance.testing.di

import android.content.Context
import androidx.room.Room
import com.frafio.myfinance.core.data.dao.ExpenseDao
import com.frafio.myfinance.core.data.dao.IncomeDao
import com.frafio.myfinance.core.data.storage.MyFinanceDatabase
import com.frafio.myfinance.core.di.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): MyFinanceDatabase =
        Room.inMemoryDatabaseBuilder(context, MyFinanceDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Provides
    fun providesExpenseDao(database: MyFinanceDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun providesIncomeDao(database: MyFinanceDatabase): IncomeDao = database.incomeDao()
}
