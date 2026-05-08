package com.guxiaopan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guxiaopan.data.local.entity.StockMyHoldingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMyHoldingDao {
    @Query("SELECT * FROM Stock_MyHolding ORDER BY stockCode ASC")
    fun observeAll(): Flow<List<StockMyHoldingEntity>>

    @Query("SELECT * FROM Stock_MyHolding ORDER BY stockCode ASC")
    suspend fun getAll(): List<StockMyHoldingEntity>

    @Query("SELECT stockCode FROM Stock_MyHolding")
    suspend fun getAllCodes(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StockMyHoldingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StockMyHoldingEntity>)

    @Query("UPDATE Stock_MyHolding SET judgmentResult = :result WHERE stockCode = :code")
    suspend fun updateJudgmentResult(code: String, result: String)

    @Query("UPDATE Stock_MyHolding SET currentPrice = :price WHERE stockCode = :code")
    suspend fun updatePrice(code: String, price: Double)

    @Query("DELETE FROM Stock_MyHolding WHERE stockCode = :code")
    suspend fun deleteByCode(code: String)

    @Query("DELETE FROM Stock_MyHolding")
    suspend fun clear()
}
