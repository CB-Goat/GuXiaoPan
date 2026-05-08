package com.guxiaopan.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 股票每日行情表
 * 对应设计文档 Stock_DailyMarket 表
 */
@Entity(
    tableName = "Stock_DailyMarket",
    indices = [
        Index(value = ["stockCode"]),
        Index(value = ["tradingDay"])
    ]
)
data class StockDailyMarketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stockCode: String,
    val stockName: String = "",
    val tradingDay: Int,               // 交易日，如 20260508
    val openingPriceToday: Double = 0.0,
    val closingPriceYesterday: Double = 0.0,
    val currentPrice: Double = 0.0,
    val percentChange: Double = 0.0,   // 涨跌幅%
    val volume: Double = 0.0,          // 成交量（手）
    val turnover: Double = 0.0,        // 成交额（元）
    val priceRange: Double = 0.0,      // 振幅%
    val maxPrice: Double = 0.0,
    val minPrice: Double = 0.0,
    val volumeRatio: Double = 0.0,     // 量比
    val turnoverRate: Double = 0.0     // 换手率%
)
