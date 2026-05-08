package com.guxiaopan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 选股条件表
 * 对应设计文档 Stock_Filters 表
 */
@Entity(tableName = "Stock_Filters")
data class StockFiltersEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val capFlag: Int = 0,              // 0-流通市值, 1-总市值
    val minCap: Double = 0.0,          // 市值最小值（亿元）
    val maxCap: Double = 0.0,          // 市值最大值（亿元）
    val ratingMonths: Int = 3,         // 评级统计月数
    val excludedIndustry: String = "", // 排除行业，逗号间隔
    val coversConcept: String = ""     // 包含概念题材，逗号间隔
)
