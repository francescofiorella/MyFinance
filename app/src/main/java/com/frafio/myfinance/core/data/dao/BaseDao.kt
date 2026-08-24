package com.frafio.myfinance.core.data.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy

interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(item: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg items: T)

    fun deleteById(id: String)

    suspend fun getById(id: String): T?

    fun getAllSync(): List<T>
}
