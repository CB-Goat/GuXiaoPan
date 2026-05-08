package com.guxiaopan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guxiaopan.data.local.entity.StockDailyMarketEntity

@Dao
interface StockDailyMarketDao {
    @Query("SELECT * FROM Stock_DailyMarket WHERE stock_code = :code ORDER BY Trading_Day DESC LIMIT :limit")
    suspend fun getRecentDays(code: String, limit: Int = 60): List<StockDailyMarketEntity>

    @Query("SELECT * FROM Stock_DailyMarket WHERE stock_code = :code ORDER BY Trading_Day DESC LIMIT 1")
    suspend fun getLatestDay(code: String): StockDailyMarketEntity?

    @Query("SELECT MAX(Trading_Day) FROM Stock_DailyMarket WHERE stock_code = :code")
    suspend fun getLastTradingDay(code: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(markets: List<StockDailyMarketEntity>)

    @Query("DELETE FROM Stock_DailyMarket WHERE Trading_Day < :beforeDay")
    suspend fun cleanOldRecords(beforeDay: Int)

    @Query("DELETE FROM Stock_DailyMarket")
    suspend fun clear()
}
