package com.guxiaopan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户授权表
 * 对应设计文档 User_Auth 表
 */
@Entity(tableName = "User_Auth")
data class UserAuthEntity(
    @PrimaryKey val phone: String,
    val authCode: String = "",
    val limitDays: Int = 0,
    val limitDate: Long = 0L  // 到期日期时间戳（毫秒）
)
