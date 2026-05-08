package com.guxiaopan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guxiaopan.data.local.entity.StockDailyMarketEntity

@Dao
interface StockDailyMarketDao {
    @Query("SELECT * FROM stock_daily_market WHERE stock_code = :code ORDER BY trading_day DESC LIMIT :limit")
    suspend fun getRecentDays(code: String, limit: Int = 60): List<StockDailyMarketEntity>

    @Query("SELECT * FROM stock_daily_market WHERE stock_code = :code ORDER BY trading_day DESC LIMIT 1")
    suspend fun getLatestDay(code: String): StockDailyMarketEntity?

    @Query("SELECT MAX(trading_day) FROM stock_daily_market WHERE stock_code = :code")
    suspend fun getLastTradingDay(code: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(markets: List<StockDailyMarketEntity>)

    @Query("DELETE FROM stock_daily_market WHERE trading_day < :beforeDay")
    suspend fun cleanOldRecords(beforeDay: Int)

    @Query("DELETE FROM stock_daily_market")
    suspend fun clear()
}
