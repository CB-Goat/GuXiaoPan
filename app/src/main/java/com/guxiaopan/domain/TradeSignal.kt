package com.guxiaopan.domain

/**
 * 交易信号枚举
 */
enum class TradeSignal {
    NONE,
    B,    // 买
    S;    // 卖

    val displayName: String get() = when (this) {
        NONE -> ""
        B -> "B"
        S -> "S"
    }
}
