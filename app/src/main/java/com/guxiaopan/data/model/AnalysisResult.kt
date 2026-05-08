package com.guxiaopan.data.model

/**
 * 分析结果
 */
data class AnalysisResult(
    val stockCode: String,
    val stockName: String,
    val signal: String = "",  // B / S / ""
    val macdState: String = "",
    val skdjState: String = ""
)
