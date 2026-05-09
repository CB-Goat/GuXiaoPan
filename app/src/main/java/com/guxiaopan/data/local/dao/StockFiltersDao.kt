package com.guxiaopan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.guxiaopan.data.local.entity.StockFiltersEntity

@Dao
interface StockFiltersDao {
    @Query("SELECT * FROM stock_filters LIMIT 1")
    suspend fun getFilter(): StockFiltersEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(filter: StockFiltersEntity)

    @Query("DELETE FROM stock_filters")
    suspend fun clear()
}
