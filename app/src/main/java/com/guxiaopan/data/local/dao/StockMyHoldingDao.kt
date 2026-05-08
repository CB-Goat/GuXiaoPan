package com.guxiaopan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guxiaopan.data.local.entity.StockMyHoldingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMyHoldingDao {
    @Query("SELECT * FROM Stock_MyHolding ORDER BY stock_code ASC")
    fun observeAll(): Flow<List<StockMyHoldingEntity>>

    @Query("SELECT * FROM Stock_MyHolding ORDER BY stock_code ASC")
    suspend fun getAll(): List<StockMyHoldingEntity>

    @Query("SELECT stock_code FROM Stock_MyHolding")
    suspend fun getAllCodes(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StockMyHoldingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StockMyHoldingEntity>)

    @Query("UPDATE Stock_MyHolding SET Judgment_Result = :result WHERE stock_code = :code")
    suspend fun updateJudgmentResult(code: String, result: String)

    @Query("UPDATE Stock_MyHolding SET current_price = :price WHERE stock_code = :code")
    suspend fun updatePrice(code: String, price: Double)

    @Query("DELETE FROM Stock_MyHolding WHERE stock_code = :code")
    suspend fun deleteByCode(code: String)

    @Query("DELETE FROM Stock_MyHolding")
    suspend fun clear()
}
