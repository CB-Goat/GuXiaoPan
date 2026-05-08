package com.guxiaopan.domain

import com.guxiaopan.common.Constants
import com.guxiaopan.data.remote.DailyBar
import kotlin.math.max

/**
 * MACD(12,26,9) + SKDJ(9,3,3) 技术指标分析
 *
 * 严格按照需求V2.0确认的判定标准：
 * - 即将进入金叉：两条现有趋势，再延伸1个交易日即可完成交叉
 * - 已进入金叉：两条已交叉，交叉后持续不超过3个交易日
 * - 即将进入死叉：两条现有趋势，再延伸1个交易日即可完成交叉
 * - 已进入死叉：两条已交叉，交叉后持续不超过3个交易日
 *
 * 综合信号：
 * - 我的关注：MACD即将金叉 + SKDJ已金叉 → B（买）
 * - 我的持仓：MACD已死叉 + SKDJ即将死叉 → S（卖）
 */
object TechnicalSignals {

    fun analyze(bars: List<DailyBar>): TradeSignal {
        if (bars.size < Constants.MIN_KLINE_COUNT) return TradeSignal.NONE

        val closes = bars.map { it.close }
        val highs = bars.map { it.high }
        val lows = bars.map { it.low }

        // 计算MACD
        val ema12 = ema(closes, Constants.MACD_FAST)
        val ema26 = ema(closes, Constants.MACD_SLOW)
        val dif = ema12.zip(ema26) { a, b -> a - b }
        val dea = ema(dif, Constants.MACD_SIGNAL)
        val hist = dif.zip(dea) { d, e -> 2.0 * (d - e) }

        // 计算SKDJ
        val (k, d) = skdjKd(highs, lows, closes)

        val last = closes.lastIndex
        if (last < 2) return TradeSignal.NONE

        // MACD状态判定
        val macdAboutGolden = isMacdAboutToGoldenCross(dif, dea, hist, last)
        val macdAlreadyGolden = isMacdAlreadyGoldenCross(dif, dea, last)
        val macdAboutDead = isMacdAboutToDeadCross(dif, dea, hist, last)
        val macdAlreadyDead = isMacdAlreadyDeadCross(dif, dea, last)

        // SKDJ状态判定
        val skdjAlreadyGolden = isSkdjAlreadyGoldenCross(k, d, last)
        val skdjAboutGolden = isSkdjAboutToGoldenCross(k, d, last)
        val skdjAlreadyDead = isSkdjAlreadyDeadCross(k, d, last)
        val skdjAboutDead = isSkdjAboutToDeadCross(k, d, last)

        // 综合信号判定
        // B信号（关注列表）：MACD即将金叉 + SKDJ已金叉
        val buy = macdAboutGolden && skdjAlreadyGolden
        // S信号（持仓列表）：MACD已死叉 + SKDJ即将死叉
        val sell = macdAlreadyDead && skdjAboutDead

        return when {
            buy && sell -> TradeSignal.NONE  // 冲突时不标记
            buy -> TradeSignal.B
            sell -> TradeSignal.S
            else -> TradeSignal.NONE
        }
    }

    // ==================== MACD 判定 ====================

    /**
     * MACD即将进入金叉
     * 条件：DIF < DEA，且趋势显示再延伸1个交易日即可交叉
     * 实现：DIF < DEA 且 DIF在上升 且 (DIF-DEA)差值在缩小 且柱状线为负但在收窄
     */
    private fun isMacdAboutToGoldenCross(
        dif: List<Double>, dea: List<Double>, hist: List<Double>, i: Int
    ): Boolean {
        if (i < 2) return false
        val difNow = dif[i]
        val deaNow = dea[i]
        val difPrev = dif[i - 1]
        val histNow = hist[i]
        val histPrev = hist[i - 1]

        // DIF在DEA下方
        if (difNow >= deaNow) return false
        // DIF在上升（趋势向上）
        if (difNow <= difPrev) return false
        // 柱状线为负但在收窄（向零轴收敛）
        if (histNow >= 0) return false
        if (histNow <= histPrev) return false

        return true
    }

    /**
     * MACD已进入金叉
     * 条件：DIF > DEA，且交叉后持续不超过3个交易日
     */
    private fun isMacdAlreadyGoldenCross(dif: List<Double>, dea: List<Double>, i: Int): Boolean {
        if (i < 1) return false
        if (dif[i] <= dea[i]) return false

        // 找到最近一次金叉位置（DIF从<=DEA变为>DEA）
        var crossIndex = i
        for (j in i downTo 1) {
            if (dif[j] > dea[j] && dif[j - 1] <= dea[j - 1]) {
                crossIndex = j
                break
            }
            if (dif[j] <= dea[j]) {
                // 还没金叉过
                return false
            }
        }

        // 金叉后持续天数不超过3天
        val sustainDays = i - crossIndex + 1
        return sustainDays <= Constants.CROSS_SUSTAIN_DAYS
    }

    /**
     * MACD即将进入死叉
     * 条件：DIF > DEA，且趋势显示再延伸1个交易日即可交叉
     */
    private fun isMacdAboutToDeadCross(
        dif: List<Double>, dea: List<Double>, hist: List<Double>, i: Int
    ): Boolean {
        if (i < 2) return false
        val difNow = dif[i]
        val deaNow = dea[i]
        val difPrev = dif[i - 1]
        val histNow = hist[i]
        val histPrev = hist[i - 1]

        // DIF在DEA上方
        if (difNow <= deaNow) return false
        // DIF在下降（趋势向下）
        if (difNow >= difPrev) return false
        // 柱状线为正但在收窄
        if (histNow <= 0) return false
        if (histNow >= histPrev) return false

        return true
    }

    /**
     * MACD已进入死叉
     * 条件：DIF < DEA，且交叉后持续不超过3个交易日
     */
    private fun isMacdAlreadyDeadCross(dif: List<Double>, dea: List<Double>, i: Int): Boolean {
        if (i < 1) return false
        if (dif[i] >= dea[i]) return false

        // 找到最近一次死叉位置
        var crossIndex = i
        for (j in i downTo 1) {
            if (dif[j] < dea[j] && dif[j - 1] >= dea[j - 1]) {
                crossIndex = j
                break
            }
            if (dif[j] >= dea[j]) {
                return false
            }
        }

        val sustainDays = i - crossIndex + 1
        return sustainDays <= Constants.CROSS_SUSTAIN_DAYS
    }

    // ==================== SKDJ 判定 ====================

    /**
     * SKDJ已进入金叉
     * 条件：K > D，且交叉后持续不超过3个交易日
     */
    private fun isSkdjAlreadyGoldenCross(k: List<Double>, d: List<Double>, i: Int): Boolean {
        if (i < 1) return false
        if (k[i] <= d[i]) return false

        var crossIndex = i
        for (j in i downTo 1) {
            if (k[j] > d[j] && k[j - 1] <= d[j - 1]) {
                crossIndex = j
                break
            }
            if (k[j] <= d[j]) return false
        }

        return (i - crossIndex + 1) <= Constants.CROSS_SUSTAIN_DAYS
    }

    /**
     * SKDJ即将进入金叉
     * 条件：K < D，趋势向上，差值收窄
     */
    private fun isSkdjAboutToGoldenCross(k: List<Double>, d: List<Double>, i: Int): Boolean {
        if (i < 1) return false
        if (k[i] >= d[i]) return false
        if (k[i] <= k[i - 1]) return false  // K在上升
        val gap = d[i] - k[i]
        val prevGap = d[i - 1] - k[i - 1]
        return gap < prevGap && gap > 0  // 差值收窄
    }

    /**
     * SKDJ已进入死叉
     * 条件：K < D，且交叉后持续不超过3个交易日
     */
    private fun isSkdjAlreadyDeadCross(k: List<Double>, d: List<Double>, i: Int): Boolean {
        if (i < 1) return false
        if (k[i] >= d[i]) return false

        var crossIndex = i
        for (j in i downTo 1) {
            if (k[j] < d[j] && k[j - 1] >= d[j - 1]) {
                crossIndex = j
                break
            }
            if (k[j] >= d[j]) return false
        }

        return (i - crossIndex + 1) <= Constants.CROSS_SUSTAIN_DAYS
    }

    /**
     * SKDJ即将进入死叉
     * 条件：K > D，趋势向下，差值收窄
     */
    private fun isSkdjAboutToDeadCross(k: List<Double>, d: List<Double>, i: Int): Boolean {
        if (i < 1) return false
        if (k[i] <= d[i]) return false
        if (k[i] >= k[i - 1]) return false  // K在下降
        val gap = k[i] - d[i]
        val prevGap = k[i - 1] - d[i - 1]
        return gap < prevGap && gap > 0  // 差值收窄
    }

    // ==================== 计算函数 ====================

    /** EMA指数移动平均 */
    private fun ema(values: List<Double>, span: Int): List<Double> {
        if (values.isEmpty()) return emptyList()
        val k = 2.0 / (span + 1)
        val out = MutableList(values.size) { 0.0 }
        out[0] = values[0]
        for (i in 1 until values.size) {
            out[i] = values[i] * k + out[i - 1] * (1 - k)
        }
        return out
    }

    /** SMA简单移动平均 */
    private fun sma(values: List<Double>, period: Int): List<Double> {
        val out = MutableList(values.size) { 0.0 }
        var sum = 0.0
        for (i in values.indices) {
            sum += values[i]
            if (i >= period) sum -= values[i - period]
            out[i] = if (i >= period - 1) sum / period else 50.0
        }
        return out
    }

    /** SKDJ的K/D计算 (9,3,3) */
    private fun skdjKd(
        high: List<Double>, low: List<Double>, close: List<Double>
    ): Pair<List<Double>, List<Double>> {
        val n = Constants.SKDJ_N
        val rsv = MutableList(close.size) { 50.0 }
        for (i in close.indices) {
            val start = max(0, i - n + 1)
            var hh = high[start]
            var ll = low[start]
            for (j in start..i) {
                hh = max(hh, high[j])
                ll = minOf(ll, low[j])
            }
            val denom = hh - ll
            rsv[i] = if (denom <= 1e-8) 50.0 else (close[i] - ll) / denom * 100.0
        }
        val k = sma(rsv, Constants.SKDJ_M1)
        val d = sma(k, Constants.SKDJ_M2)
        return k to d
    }
}
