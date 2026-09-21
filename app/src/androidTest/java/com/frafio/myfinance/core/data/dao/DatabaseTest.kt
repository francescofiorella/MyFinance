package com.frafio.myfinance.core.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.frafio.myfinance.core.data.storage.MyFinanceDatabase
import org.junit.After
import org.junit.Before

/** A fresh in-memory database per test; bypasses the app's singleton and its migration callback. */
internal abstract class DatabaseTest {

    private lateinit var db: MyFinanceDatabase
    protected lateinit var expenseDao: ExpenseDao
    protected lateinit var incomeDao: IncomeDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyFinanceDatabase::class.java,
        ).build()
        expenseDao = db.expenseDao()
        incomeDao = db.incomeDao()
    }

    @After
    fun teardown() = db.close()
}
