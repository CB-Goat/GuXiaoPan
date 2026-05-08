package com.guxiaopan.common

/**
 * 全局常量定义
 */
object Constants {
    // MACD 参数（固定）
    const val MACD_FAST = 12
    const val MACD_SLOW = 26
    const val MACD_SIGNAL = 9

    // SKDJ 参数（固定）
    const val SKDJ_N = 9
    const val SKDJ_M1 = 3
    const val SKDJ_M2 = 3

    // 金叉/死叉持续天数阈值
    const val CROSS_SUSTAIN_DAYS = 3

    // 数据刷新间隔（毫秒）- 10分钟
    const val REFRESH_INTERVAL_MS = 10 * 60 * 1000L

    // 爬虫请求间隔（毫秒）
    const val CRAWL_DELAY_MS = 200L

    // K线分析最少需要的数据量
    const val MIN_KLINE_COUNT = 40

    // K线获取数量
    const val KLINE_FETCH_COUNT = 60

    // 默认体验天数
    const val DEFAULT_TRIAL_DAYS = 30

    // 联系电话
    const val CONTACT_PHONE = "18674827052"

    // 默认排除行业
    val DEFAULT_EXCLUDED_INDUSTRIES = listOf("电商", "物流", "餐饮", "金融", "地产")

    // 股票flag定义
    const val FLAG_NORMAL = 0
    const val FLAG_NEW = 1
    const val FLAG_WARNING = 2
    const val FLAG_DELISTED = 3

    // 信号定义
    const val SIGNAL_BUY = "B"
    const val SIGNAL_SELL = "S"
    const val SIGNAL_NONE = ""

    // 断点续传SharedPreferences key
    const val PREFS_CRAWL = "crawl_progress"
    const val KEY_CRAWL_LAST_INDEX = "last_stock_index"
    const val KEY_CRAWL_TOTAL = "crawl_total"
    const val KEY_CRAWL_STATUS = "crawl_status"
}