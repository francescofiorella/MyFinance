package com.frafio.myfinance.di

import android.content.Context
import com.frafio.myfinance.data.dao.ExpenseDao
import com.frafio.myfinance.data.dao.IncomeDao
import com.frafio.myfinance.data.storage.MyFinanceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MyFinanceDatabase {
        return MyFinanceDatabase.getDatabase(context)
    }

    @Provides
    fun provideExpenseDao(database: MyFinanceDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideIncomeDao(database: MyFinanceDatabase): IncomeDao {
        return database.incomeDao()
    }
}
