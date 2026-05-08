package com.guxiaopan.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_info",
    indices = [
        Index(value = ["flag"]),
        Index(value = ["industry"])
    ]
)
data class StockInfoEntity(
    @PrimaryKey @ColumnInfo(name = "stock_code") val stockCode: String,
    @ColumnInfo(name = "stock_name") val stockName: String = "",
    val industry: String = "",
    val concept: String = "",
    @ColumnInfo(name = "analyst_rating") val analystRating: String = "",
    @ColumnInfo(name = "circulating_market_cap") val circulatingMarketCap: String = "",
    @ColumnInfo(name = "market_cap") val marketCap: String = "",
    val flag: Int = 0
)
