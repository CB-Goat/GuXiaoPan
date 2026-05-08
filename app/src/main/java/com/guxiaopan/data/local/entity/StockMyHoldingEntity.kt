package com.guxiaopan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 我的持仓表
 * 对应设计文档 Stock_MyHolding 表
 */
@Entity(tableName = "Stock_MyHolding")
data class StockMyHoldingEntity(
    @PrimaryKey val stockCode: String,
    val stockName: String = "",
    val industry: String = "",
    val concept: String = "",
    val analystRating: String = "",
    val circulatingMarketCap: String = "",
    val marketCap: String = "",
    val currentPrice: Double = 0.0,
    val holdings: Int = 0,             // 持仓数量
    val judgmentResult: String = ""    // B/S/空
)
