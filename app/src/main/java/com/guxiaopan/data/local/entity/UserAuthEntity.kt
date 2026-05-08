package com.guxiaopan.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_auth")
data class UserAuthEntity(
    @PrimaryKey val phone: String,
    @ColumnInfo(name = "auth_code") val authCode: String = "",
    @ColumnInfo(name = "limit_days") val limitDays: Int = 0,
    @ColumnInfo(name = "limit_date") val limitDate: Long = 0L
)
