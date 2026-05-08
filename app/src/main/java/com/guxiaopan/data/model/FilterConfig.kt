package com.guxiaopan.data.model

/**
 * 选股条件配置
 */
data class FilterConfig(
    val capFlag: Int = 0,              // 0-流通市值, 1-总市值
    val minCap: Double = 0.0,          // 市值最小值（亿元）
    val maxCap: Double = 0.0,          // 市值最大值（亿元）
    val ratingMonths: Int = 3,         // 评级统计月数
    val excludedIndustries: Set<String> = emptySet(),  // 排除行业
    val coversConcepts: Set<String> = emptySet()       // 包含概念题材
)
