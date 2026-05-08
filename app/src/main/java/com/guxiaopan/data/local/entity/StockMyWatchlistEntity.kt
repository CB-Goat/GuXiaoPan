package com.guxiaopan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 我的关注表
 * 对应设计文档 Stock_MyWatchlist 表
 */
@Entity(tableName = "Stock_MyWatchlist")
data class StockMyWatchlistEntity(
    @PrimaryKey val stockCode: String,
    val stockName: String = "",
    val industry: String = "",
    val concept: String = "",
    val analystRating: String = "",
    val circulatingMarketCap: String = "",
    val marketCap: String = "",
    val currentPrice: Double = 0.0,
    val judgmentResult: String = ""    // B/S/空
)
