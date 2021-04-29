package com.frafio.myfinance.data.db

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.db.entities.User

interface UserDao {
    fun getUser(): LiveData<User>

    fun updateUser(user: User)
}