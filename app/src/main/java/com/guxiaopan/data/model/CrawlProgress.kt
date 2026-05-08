package com.guxiaopan.data.model

/**
 * 数据抓取进度
 */
data class CrawlProgress(
    val total: Int = 0,
    val completed: Int = 0,
    val currentTask: String = "",
    val status: CrawlStatus = CrawlStatus.IDLE,
    val percent: Int = 0
) {
    companion object {
        fun idle() = CrawlProgress(status = CrawlStatus.IDLE)
    }
}

enum class CrawlStatus {
    IDLE,           // 空闲
    CRAWLING,       // 抓取中
    PAUSED,         // 暂停（断点）
    COMPLETED,      // 完成
    FAILED          // 失败
}
