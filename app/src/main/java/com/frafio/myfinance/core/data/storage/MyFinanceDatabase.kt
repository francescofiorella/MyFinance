package com.frafio.myfinance.core.data.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.frafio.myfinance.core.data.converters.Converters
import com.frafio.myfinance.core.data.dao.IncomeDao
import com.frafio.myfinance.core.data.dao.ExpenseDao
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.model.Expense

/**
 * DB Versions:
 * 1 - Initial version
 * 2 - "labels" attribute added to Expense and Income
 */
@Database(
    entities = [Expense::class, Income::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MyFinanceDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao

    companion object {
        @Volatile
        private var INSTANCE: MyFinanceDatabase? = null

        fun getDatabase(context: Context): MyFinanceDatabase =
            (INSTANCE ?: synchronized(this) {
                val i = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MyFinanceDatabase::class.java,
                    "myFinanceLocal"
                )
                    .fallbackToDestructiveMigration(true)
                    .createFromAsset("database/myFinanceDatabase.db")
                    .build()
                INSTANCE = i
                INSTANCE
            })!!
    }
}