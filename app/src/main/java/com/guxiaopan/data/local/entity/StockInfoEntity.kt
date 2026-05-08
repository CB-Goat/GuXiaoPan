package com.guxiaopan.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 股票基本信息表
 * 对应设计文档 Stock_Info 表
 * flag: 0-正常, 1-新股, 2-警示股, 3-退市股
 */
@Entity(
    tableName = "Stock_Info",
    indices = [
        Index(value = ["flag"]),
        Index(value = ["industry"])
    ]
)
data class StockInfoEntity(
    @PrimaryKey val stockCode: String,
    val stockName: String = "",
    val industry: String = "",
    val concept: String = "",           // 概念题材，分号间隔
    val analystRating: String = "",     // 机构评级 "3M-买入1，增持2；6M-买入3，增持3"
    val circulatingMarketCap: String = "", // 流通市值（原始字符串，如"123.45亿"）
    val marketCap: String = "",         // 总市值
    val flag: Int = 0
)
