package com.guxiaopan.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_my_holding")
data class StockMyHoldingEntity(
    @PrimaryKey @ColumnInfo(name = "stock_code") val stockCode: String,
    @ColumnInfo(name = "stock_name") val stockName: String = "",
    val industry: String = "",
    val concept: String = "",
    @ColumnInfo(name = "analyst_rating") val analystRating: String = "",
    @ColumnInfo(name = "circulating_market_cap") val circulatingMarketCap: String = "",
    @ColumnInfo(name = "market_cap") val marketCap: String = "",
    @ColumnInfo(name = "current_price") val currentPrice: Double = 0.0,
    val holdings: Int = 0,
    @ColumnInfo(name = "judgment_result") val judgmentResult: String = ""
)
