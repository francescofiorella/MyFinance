package com.frafio.myfinance.core.data.converters

import androidx.room.TypeConverter
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return JSONArray(value).toString()
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val list = mutableListOf<String>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        return list
    }
}
