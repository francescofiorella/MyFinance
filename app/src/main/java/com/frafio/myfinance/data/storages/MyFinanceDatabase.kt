package com.frafio.myfinance.data.storages

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.frafio.myfinance.data.daos.IncomeDao
import com.frafio.myfinance.data.daos.PurchaseDao
import com.frafio.myfinance.data.models.Income
import com.frafio.myfinance.data.models.Purchase

@Database(
    entities = [Purchase::class, Income::class],
    version = 1,
    exportSchema = false
)
abstract class MyFinanceDatabase : RoomDatabase() {
    abstract fun purchaseDao(): PurchaseDao
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
                    //.allowMainThreadQueries()
                    .createFromAsset("database/myFinanceDatabase.db").build()
                INSTANCE = i
                INSTANCE
            })!!
    }
}