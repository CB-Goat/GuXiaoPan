package com.guxiaopan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guxiaopan.data.local.entity.StockMyHoldingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMyHoldingDao {
    @Query("SELECT * FROM stock_my_holding ORDER BY stock_code ASC")
    fun observeAll(): Flow<List<StockMyHoldingEntity>>

    @Query("SELECT * FROM stock_my_holding ORDER BY stock_code ASC")
    suspend fun getAll(): List<StockMyHoldingEntity>

    @Query("SELECT stock_code FROM stock_my_holding")
    suspend fun getAllCodes(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StockMyHoldingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StockMyHoldingEntity>)

    @Query("UPDATE stock_my_holding SET judgment_result = :result WHERE stock_code = :code")
    suspend fun updateJudgmentResult(code: String, result: String)

    @Query("UPDATE stock_my_holding SET current_price = :price WHERE stock_code = :code")
    suspend fun updatePrice(code: String, price: Double)

    @Query("DELETE FROM stock_my_holding WHERE stock_code = :code")
    suspend fun deleteByCode(code: String)

    @Query("DELETE FROM stock_my_holding")
    suspend fun clear()
}
