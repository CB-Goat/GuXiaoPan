package com.guxiaopan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guxiaopan.data.local.entity.UserAuthEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAuthDao {
    @Query("SELECT * FROM user_auth LIMIT 1")
    fun observeUser(): Flow<UserAuthEntity?>

    @Query("SELECT * FROM user_auth WHERE phone = :phone LIMIT 1")
    suspend fun getUser(phone: String): UserAuthEntity?

    @Query("SELECT * FROM user_auth LIMIT 1")
    suspend fun getAnyUser(): UserAuthEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserAuthEntity)

    @Query("UPDATE user_auth SET auth_code = :authCode, limit_days = :days, limit_date = :expiry WHERE phone = :phone")
    suspend fun updateAuth(phone: String, authCode: String, days: Int, expiry: Long)

    @Query("DELETE FROM user_auth")
    suspend fun clear()
}
