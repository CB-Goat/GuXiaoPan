package com.guxiaopan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guxiaopan.data.local.entity.StockMyWatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMyWatchlistDao {
    @Query("SELECT * FROM Stock_MyWatchlist ORDER BY stockCode ASC")
    fun observeAll(): Flow<List<StockMyWatchlistEntity>>

    @Query("SELECT * FROM Stock_MyWatchlist ORDER BY stockCode ASC")
    suspend fun getAll(): List<StockMyWatchlistEntity>

    @Query("SELECT stockCode FROM Stock_MyWatchlist")
    suspend fun getAllCodes(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StockMyWatchlistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StockMyWatchlistEntity>)

    @Query("UPDATE Stock_MyWatchlist SET judgmentResult = :result WHERE stockCode = :code")
    suspend fun updateJudgmentResult(code: String, result: String)

    @Query("UPDATE Stock_MyWatchlist SET currentPrice = :price WHERE stockCode = :code")
    suspend fun updatePrice(code: String, price: Double)

    @Query("DELETE FROM Stock_MyWatchlist WHERE stockCode NOT IN (:keepCodes)")
    suspend fun deleteExcept(keepCodes: List<String>)

    @Query("DELETE FROM Stock_MyWatchlist WHERE stockCode = :code")
    suspend fun deleteByCode(code: String)

    @Query("DELETE FROM Stock_MyWatchlist")
    suspend fun clear()
}
