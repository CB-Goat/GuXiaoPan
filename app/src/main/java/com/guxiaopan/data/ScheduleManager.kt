package com.guxiaopan.data

import com.guxiaopan.common.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 定时刷新管理器
 * 交易日每10分钟自动刷新实时行情
 */
class ScheduleManager(
    private val repository: StockRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    private var refreshJob: Job? = null

    /**
     * 启动定时刷新
     */
    fun startPeriodicRefresh() {
        stopPeriodicRefresh()
        refreshJob = scope.launch {
            while (isActive) {
                delay(Constants.REFRESH_INTERVAL_MS)
                if (!isTradingTime()) continue
                try {
                    repository.refreshRealtimeQuotes()
                } catch (_: Exception) {
                    // 静默失败，不影响用户
                }
            }
        }
    }

    /**
     * 停止定时刷新
     */
    fun stopPeriodicRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    /**
     * 判断当前是否在交易时间（简化版：周一到周五 9:30-15:00）
     */
    private fun isTradingTime(): Boolean {
        val now = java.util.Calendar.getInstance()
        val dayOfWeek = now.get(java.util.Calendar.DAY_OF_WEEK)
        // 周日=1, 周六=7
        if (dayOfWeek == 1 || dayOfWeek == 7) return false

        val hour = now.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = now.get(java.util.Calendar.MINUTE)
        val totalMinutes = hour * 60 + minute

        // 9:30 - 11:30, 13:00 - 15:00
        return (totalMinutes in 570..690) || (totalMinutes in 780..900)
    }
}
