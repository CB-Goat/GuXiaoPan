package com.guxiaopan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guxiaopan.data.local.entity.StockMyWatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMyWatchlistDao {
    @Query("SELECT * FROM stock_my_watchlist ORDER BY stock_code ASC")
    fun observeAll(): Flow<List<StockMyWatchlistEntity>>

    @Query("SELECT * FROM stock_my_watchlist ORDER BY stock_code ASC")
    suspend fun getAll(): List<StockMyWatchlistEntity>

    @Query("SELECT stock_code FROM stock_my_watchlist")
    suspend fun getAllCodes(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StockMyWatchlistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StockMyWatchlistEntity>)

    @Query("UPDATE stock_my_watchlist SET judgment_result = :result WHERE stock_code = :code")
    suspend fun updateJudgmentResult(code: String, result: String)

    @Query("UPDATE stock_my_watchlist SET current_price = :price WHERE stock_code = :code")
    suspend fun updatePrice(code: String, price: Double)

    @Query("DELETE FROM stock_my_watchlist WHERE stock_code NOT IN (:keepCodes)")
    suspend fun deleteExcept(keepCodes: List<String>)

    @Query("DELETE FROM stock_my_watchlist WHERE stock_code = :code")
    suspend fun deleteByCode(code: String)

    @Query("DELETE FROM stock_my_watchlist")
    suspend fun clear()
}
