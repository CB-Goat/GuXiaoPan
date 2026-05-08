package com.guxiaopan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guxiaopan.data.local.entity.UserAuthEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAuthDao {
    @Query("SELECT * FROM User_Auth LIMIT 1")
    fun observeUser(): Flow<UserAuthEntity?>

    @Query("SELECT * FROM User_Auth WHERE phone = :phone LIMIT 1")
    suspend fun getUser(phone: String): UserAuthEntity?

    @Query("SELECT * FROM User_Auth LIMIT 1")
    suspend fun getAnyUser(): UserAuthEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserAuthEntity)

    @Query("UPDATE User_Auth SET authCode = :authCode, limitDays = :days, limitDate = :expiry WHERE phone = :phone")
    suspend fun updateAuth(phone: String, authCode: String, days: Int, expiry: Long)

    @Query("DELETE FROM User_Auth")
    suspend fun clear()
}
