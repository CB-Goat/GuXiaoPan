package com.guxiaopan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.guxiaopan.data.local.dao.StockDailyMarketDao
import com.guxiaopan.data.local.dao.StockFiltersDao
import com.guxiaopan.data.local.dao.StockInfoDao
import com.guxiaopan.data.local.dao.StockMyHoldingDao
import com.guxiaopan.data.local.dao.StockMyWatchlistDao
import com.guxiaopan.data.local.dao.UserAuthDao
import com.guxiaopan.data.local.entity.StockDailyMarketEntity
import com.guxiaopan.data.local.entity.StockFiltersEntity
import com.guxiaopan.data.local.entity.StockInfoEntity
import com.guxiaopan.data.local.entity.StockMyHoldingEntity
import com.guxiaopan.data.local.entity.StockMyWatchlistEntity
import com.guxiaopan.data.local.entity.UserAuthEntity

@Database(
    entities = [
        UserAuthEntity::class,
        StockInfoEntity::class,
        StockDailyMarketEntity::class,
        StockMyHoldingEntity::class,
        StockMyWatchlistEntity::class,
        StockFiltersEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userAuthDao(): UserAuthDao
    abstract fun stockInfoDao(): StockInfoDao
    abstract fun stockDailyMarketDao(): StockDailyMarketDao
    abstract fun stockMyHoldingDao(): StockMyHoldingDao
    abstract fun stockMyWatchlistDao(): StockMyWatchlistDao
    abstract fun stockFiltersDao(): StockFiltersDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun create(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "guxiaopan.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
