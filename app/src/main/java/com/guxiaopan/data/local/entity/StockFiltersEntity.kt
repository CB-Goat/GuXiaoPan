package com.guxiaopan.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_filters")
data class StockFiltersEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "cap_flag") val capFlag: Int = 0,
    @ColumnInfo(name = "min_cap") val minCap: Double = 0.0,
    @ColumnInfo(name = "max_cap") val maxCap: Double = 0.0,
    @ColumnInfo(name = "rating_months") val ratingMonths: Int = 3,
    @ColumnInfo(name = "excluded_industry") val excludedIndustry: String = "",
    @ColumnInfo(name = "covers_concept") val coversConcept: String = ""
)
