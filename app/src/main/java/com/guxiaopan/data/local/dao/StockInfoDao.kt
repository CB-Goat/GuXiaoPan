package com.guxiaopan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guxiaopan.data.local.entity.StockInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockInfoDao {
    @Query("SELECT * FROM Stock_Info WHERE flag = 0")
    suspend fun getNormalStocks(): List<StockInfoEntity>

    @Query("SELECT * FROM Stock_Info WHERE stock_code = :code")
    suspend fun getStock(code: String): StockInfoEntity?

    @Query("SELECT COUNT(*) FROM Stock_Info")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM Stock_Info WHERE flag = 0")
    suspend fun countNormal(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<StockInfoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: StockInfoEntity)

    @Query("UPDATE Stock_Info SET flag = :flag WHERE stock_code IN (:codes)")
    suspend fun updateFlag(codes: List<String>, flag: Int)

    @Query("UPDATE Stock_Info SET concept = :concept WHERE stock_code = :code")
    suspend fun updateConcept(code: String, concept: String)

    @Query("UPDATE Stock_Info SET Analyst_Rating = :rating WHERE stock_code = :code")
    suspend fun updateRating(code: String, rating: String)

    @Query("DELETE FROM Stock_Info")
    suspend fun clear()

    @Query("SELECT DISTINCT industry FROM Stock_Info WHERE industry != '' ORDER BY industry")
    suspend fun getAllIndustries(): List<String>

    @Query("SELECT DISTINCT concept FROM Stock_Info WHERE concept != '' LIMIT 1")
    suspend fun getSampleConcepts(): String?
}
