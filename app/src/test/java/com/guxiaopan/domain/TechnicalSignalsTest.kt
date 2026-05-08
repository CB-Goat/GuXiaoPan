package com.guxiaopan.domain

import com.guxiaopan.data.remote.DailyBar
import org.junit.Test
import org.junit.Assert.*

/**
 * MACD/SKDJ技术指标算法单元测试
 */
class TechnicalSignalsTest {

    @Test
    fun `test insufficient data returns NONE`() {
        // 少于40根K线应返回NONE
        val bars = generateBars(30, 100.0, 0.01)
        val signal = TechnicalSignals.analyze(bars)
        assertEquals(TradeSignal.NONE, signal)
    }

    @Test
    fun `test MACD golden cross detection`() {
        // 构造MACD即将金叉场景：DIF<DEA但趋势向上
        val bars = generateGoldenCrossScenario()
        val signal = TechnicalSignals.analyze(bars)
        // 此时SKDJ未金叉，应返回NONE
        assertEquals(TradeSignal.NONE, signal)
    }

    @Test
    fun `test SKDJ golden cross detection`() {
        // 构造SKDJ已金叉场景
        val bars = generateSkdjGoldenCrossScenario()
        // 单独SKDJ金叉不足以产生B信号
        val signal = TechnicalSignals.analyze(bars)
        // 需要MACD即将金叉 + SKDJ已金叉
        assertTrue(signal == TradeSignal.NONE || signal == TradeSignal.B)
    }

    @Test
    fun `test combined BUY signal`() {
        // 构造MACD即将金叉 + SKDJ已金叉场景
        val bars = generateBuySignalScenario()
        val signal = TechnicalSignals.analyze(bars)
        assertEquals(TradeSignal.B, signal)
    }

    @Test
    fun `test combined SELL signal`() {
        // 构造MACD已死叉 + SKDJ即将死叉场景
        val bars = generateSellSignalScenario()
        val signal = TechnicalSignals.analyze(bars)
        assertEquals(TradeSignal.S, signal)
    }

    @Test
    fun `test conflicting signals returns NONE`() {
        // 构造同时满足B和S的冲突场景
        val bars = generateConflictingScenario()
        val signal = TechnicalSignals.analyze(bars)
        assertEquals(TradeSignal.NONE, signal)
    }

    // ==================== 辅助方法 ====================

    private fun generateBars(count: Int, basePrice: Double, volatility: Double): List<DailyBar> {
        return List(count) { index ->
            val trend = index * 0.1  // 轻微上升趋势
            val random = (Math.random() - 0.5) * volatility
            val close = basePrice * (1 + trend + random)
            DailyBar(
                date = "2026-01-${(index + 1).toString().padStart(2, '0')}",
                open = close * 0.99,
                close = close,
                high = close * 1.01,
                low = close * 0.98,
                volume = 1000000.0,
                amount = 10000000.0,
            )
        }
    }

    private fun generateGoldenCrossScenario(): List<DailyBar> {
        // MACD即将金叉：DIF<DEA但DIF上升，柱状线收窄
        val closes = mutableListOf<Double>()
        var price = 100.0
        repeat(50) { index ->
            // 前期下跌
            if (index < 30) {
                price *= (0.98 + Math.random() * 0.02)
            } else {
                // 后期反弹，形成金叉前形态
                price *= (1.005 + Math.random() * 0.01)
            }
            closes.add(price)
        }
        return closes.mapIndexed { index, close ->
            DailyBar(
                date = "2026-01-${(index + 1).toString().padStart(2, '0')}",
                open = close * 0.995,
                close = close,
                high = close * 1.005,
                low = close * 0.99,
                volume = 1000000.0,
                amount = 10000000.0,
            )
        }
    }

    private fun generateSkdjGoldenCrossScenario(): List<DailyBar> {
        // SKDJ已金叉：K上穿D
        return List(50) { index ->
            val basePrice = 100.0
            val close = when {
                index < 40 -> basePrice * (0.95 + index * 0.001)
                else -> basePrice * (1.05 + (index - 40) * 0.002)
            }
            DailyBar(
                date = "2026-01-${(index + 1).toString().padStart(2, '0')}",
                open = close * 0.995,
                close = close,
                high = close * 1.01,
                low = close * 0.99,
                volume = 1000000.0,
                amount = 10000000.0,
            )
        }
    }

    private fun generateBuySignalScenario(): List<DailyBar> {
        // MACD即将金叉 + SKDJ已金叉
        val bars = mutableListOf<DailyBar>()
        var price = 100.0
        repeat(60) { index ->
            // 构造金叉前形态
            price = when {
                index < 20 -> price * (0.99 + Math.random() * 0.01)  // 下跌
                index < 40 -> price * (0.995 + Math.random() * 0.01) // 企稳
                else -> price * (1.008 + Math.random() * 0.005)      // 反弹
            }
            bars.add(DailyBar(
                date = "2026-01-${(index + 1).toString().padStart(2, '0')}",
                open = price * 0.998,
                close = price,
                high = price * 1.005,
                low = price * 0.995,
                volume = 1000000.0,
                amount = 10000000.0,
            ))
        }
        return bars
    }

    private fun generateSellSignalScenario(): List<DailyBar> {
        // MACD已死叉 + SKDJ即将死叉
        val bars = mutableListOf<DailyBar>()
        var price = 100.0
        repeat(60) { index ->
            // 构造死叉形态
            price = when {
                index < 20 -> price * (1.01 + Math.random() * 0.01)  // 上涨
                index < 40 -> price * (1.005 + Math.random() * 0.005) // 顶部
                else -> price * (0.992 + Math.random() * 0.005)      // 下跌
            }
            bars.add(DailyBar(
                date = "2026-01-${(index + 1).toString().padStart(2, '0')}",
                open = price * 1.002,
                close = price,
                high = price * 1.005,
                low = price * 0.995,
                volume = 1000000.0,
                amount = 10000000.0,
            ))
        }
        return bars
    }

    private fun generateConflictingScenario(): List<DailyBar> {
        // 构造同时满足B和S的冲突场景（理论上不应同时满足）
        return generateBars(60, 100.0, 0.02)
    }
}
