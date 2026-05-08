package com.guxiaopan.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_daily_market",
    indices = [
        Index(value = ["stock_code"]),
        Index(value = ["trading_day"])
    ]
)
data class StockDailyMarketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "stock_code") val stockCode: String,
    @ColumnInfo(name = "stock_name") val stockName: String = "",
    @ColumnInfo(name = "trading_day") val tradingDay: Int,
    @ColumnInfo(name = "opening_price_today") val openingPriceToday: Double = 0.0,
    @ColumnInfo(name = "closing_price_yesterday") val closingPriceYesterday: Double = 0.0,
    @ColumnInfo(name = "current_price") val currentPrice: Double = 0.0,
    @ColumnInfo(name = "percent_change") val percentChange: Double = 0.0,
    val volume: Double = 0.0,
    val turnover: Double = 0.0,
    @ColumnInfo(name = "price_range") val priceRange: Double = 0.0,
    @ColumnInfo(name = "max_price") val maxPrice: Double = 0.0,
    @ColumnInfo(name = "min_price") val minPrice: Double = 0.0,
    @ColumnInfo(name = "volume_ratio") val volumeRatio: Double = 0.0,
    @ColumnInfo(name = "turnover_rate") val turnoverRate: Double = 0.0
)
