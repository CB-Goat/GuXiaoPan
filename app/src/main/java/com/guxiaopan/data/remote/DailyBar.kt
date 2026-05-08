package com.guxiaopan.data.remote

/**
 * 日K线数据模型
 */
data class DailyBar(
    val date: String,       // 日期，如 "2026-05-08"
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Double,
    val amount: Double,
    val changePercent: Double = 0.0,  // 涨跌幅%
    val turnoverRate: Double = 0.0,   // 换手率%
    val volumeRatio: Double = 0.0,    // 量比
)
