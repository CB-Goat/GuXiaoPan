package com.guxiaopan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guxiaopan.data.local.entity.StockInfoEntity

@Dao
interface StockInfoDao {
    @Query("SELECT * FROM stock_info WHERE flag = 0")
    suspend fun getNormalStocks(): List<StockInfoEntity>

    @Query("SELECT * FROM stock_info WHERE stock_code = :code")
    suspend fun getStock(code: String): StockInfoEntity?

    @Query("SELECT COUNT(*) FROM stock_info")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM stock_info WHERE flag = 0")
    suspend fun countNormal(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<StockInfoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: StockInfoEntity)

    @Query("UPDATE stock_info SET flag = :flag WHERE stock_code IN (:codes)")
    suspend fun updateFlag(codes: List<String>, flag: Int)

    @Query("UPDATE stock_info SET concept = :concept WHERE stock_code = :code")
    suspend fun updateConcept(code: String, concept: String)

    @Query("UPDATE stock_info SET analyst_rating = :rating WHERE stock_code = :code")
    suspend fun updateRating(code: String, rating: String)

    @Query("DELETE FROM stock_info")
    suspend fun clear()

    @Query("SELECT DISTINCT industry FROM stock_info WHERE industry != '' ORDER BY industry")
    suspend fun getAllIndustries(): List<String>

    @Query("SELECT DISTINCT concept FROM stock_info WHERE concept != '' LIMIT 1")
    suspend fun getSampleConcepts(): String?
}
