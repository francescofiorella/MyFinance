package com.frafio.myfinance.testing.util

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.frafio.myfinance.core.data.dao.ExpenseDao
import com.frafio.myfinance.core.data.dao.IncomeDao
import com.frafio.myfinance.core.data.storage.MyFinanceDatabase
import org.junit.After
import org.junit.Before

/**
 * A fresh in-memory database per test; bypasses the app's singleton and its migration callback.
 * Used by the DAO tests on the device and, under Robolectric, by the sync-manager tests.
 */
abstract class DatabaseTest {

    protected lateinit var db: MyFinanceDatabase
        private set
    protected lateinit var expenseDao: ExpenseDao
    protected lateinit var incomeDao: IncomeDao

    @Before
    fun setupDatabase() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyFinanceDatabase::class.java,
        ).build()
        expenseDao = db.expenseDao()
        incomeDao = db.incomeDao()
    }

    @After
    fun closeDatabase() = db.close()
}
