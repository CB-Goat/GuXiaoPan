package com.guxiaopan.data

import android.content.Context
import android.content.SharedPreferences
import com.guxiaopan.common.Constants
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
import com.guxiaopan.data.model.AnalysisResult
import com.guxiaopan.data.model.CrawlProgress
import com.guxiaopan.data.model.CrawlStatus
import com.guxiaopan.data.model.FilterConfig
import com.guxiaopan.data.remote.DailyBar
import com.guxiaopan.data.remote.EastMoneyClient
import com.guxiaopan.data.remote.SinaClient
import com.guxiaopan.data.remote.StockListRow
import com.guxiaopan.domain.TechnicalSignals
import com.guxiaopan.util.StockCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * 股票数据仓库 - 核心数据层
 * 管理所有数据的获取、存储、分析
 */
class StockRepository(
    private val db: AppDatabase,
    private val context: Context,
    private val east: EastMoneyClient = EastMoneyClient(),
    private val sina: SinaClient = SinaClient(),
) {
    // DAO
    private val userAuthDao get() = db.userAuthDao()
    private val stockInfoDao get() = db.stockInfoDao()
    private val dailyMarketDao get() = db.stockDailyMarketDao()
    private val holdingDao get() = db.stockMyHoldingDao()
    private val watchlistDao get() = db.stockMyWatchlistDao()
    private val filtersDao get() = db.stockFiltersDao()

    // 断点续传
    private val crawlPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(Constants.PREFS_CRAWL, Context.MODE_PRIVATE)
    }

    // 抓取进度
    private val _crawlProgress = MutableStateFlow(CrawlProgress.idle())
    val crawlProgress: Flow<CrawlProgress> = _crawlProgress.asStateFlow()

    // ==================== 用户授权 ====================

    fun observeUser() = userAuthDao.observeUser()

    suspend fun getUser() = userAuthDao.getAnyUser()

    suspend fun login(phone: String): UserAuthEntity {
        val normalized = phone.trim()
        val existing = userAuthDao.getUser(normalized)
        if (existing != null) return existing
        val now = System.currentTimeMillis()
        val expiry = now + Constants.DEFAULT_TRIAL_DAYS * 24 * 60 * 60 * 1000L
        val user = UserAuthEntity(
            phone = normalized,
            limitDays = Constants.DEFAULT_TRIAL_DAYS,
            limitDate = expiry
        )
        userAuthDao.insert(user)
        return user
    }

    suspend fun updateAuth(phone: String, authCode: String, days: Int, expiry: Long) {
        userAuthDao.updateAuth(phone, authCode, days, expiry)
    }

    // ==================== 数据抓取 ====================

    suspend fun startFullCrawl(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            _crawlProgress.value = CrawlProgress(status = CrawlStatus.CRAWLING, currentTask = "开始抓取A股列表…")

            // Step 1: 抓取全部A股基本信息
            crawlAllStocks()

            // Step 2: 抓取每日行情数据（增量）
            _crawlProgress.value = _crawlProgress.value.copy(currentTask = "抓取行情数据…")
            crawlDailyMarket()

            // Step 3: 标记新股/退市股
            _crawlProgress.value = _crawlProgress.value.copy(currentTask = "标记新股/退市股…")
            markSpecialStocks()

            // 清除断点记录
            clearCrawlState()
            _crawlProgress.value = CrawlProgress(total = 100, completed = 100, status = CrawlStatus.COMPLETED, percent = 100)
        }.onFailure {
            _crawlProgress.value = _crawlProgress.value.copy(status = CrawlStatus.FAILED)
        }
    }

    private suspend fun crawlAllStocks() {
        val allStocks = mutableListOf<StockListRow>()
        var page = 1
        while (true) {
            delay(Constants.CRAWL_DELAY_MS)
            val rows = east.fetchStockPage(page, 500).getOrElse { break }
            if (rows.isEmpty()) break
            allStocks.addAll(rows)
            val percent = minOf(99, (page * 500 * 30 / 100))
            _crawlProgress.value = _crawlProgress.value.copy(
                completed = allStocks.size,
                total = allStocks.size + 500,
                percent = percent,
                currentTask = "抓取A股列表 第${page}页…"
            )
            // 保存断点
            saveCrawlState("stock_list", page)
            page++
            if (rows.size < 500) break
        }

        // 批量写入数据库
        val entities = allStocks.map { row ->
            StockInfoEntity(
                stockCode = row.code,
                stockName = row.name,
                industry = row.industry,
                circulatingMarketCap = if (row.circulatingMarketCapYi > 0) "${"%.2f".format(row.circulatingMarketCapYi)}亿" else "",
                marketCap = if (row.totalMarketCapYi > 0) "${"%.2f".format(row.totalMarketCapYi)}亿" else "",
            )
        }
        stockInfoDao.insertAll(entities)
    }

    private suspend fun crawlDailyMarket() {
        val stocks = stockInfoDao.getNormalStocks()
        val total = stocks.size
        stocks.forEachIndexed { index, stock ->
            delay(Constants.CRAWL_DELAY_MS)
            val bars = fetchKline(stock.stockCode).getOrElse { emptyList() }
            if (bars.isNotEmpty()) {
                val entities = bars.map { bar ->
                    StockDailyMarketEntity(
                        stockCode = stock.stockCode,
                        stockName = stock.stockName,
                        tradingDay = bar.date.replace("-", "").toIntOrNull() ?: 0,
                        openingPriceToday = bar.open,
                        closingPriceYesterday = if (bars.size > 1) bars[bars.lastIndex - 1].close else bar.open,
                        currentPrice = bar.close,
                        percentChange = bar.changePercent,
                        volume = bar.volume,
                        turnover = bar.amount,
                        maxPrice = bar.high,
                        minPrice = bar.low,
                        turnoverRate = bar.turnoverRate,
                        volumeRatio = bar.volumeRatio,
                    )
                }
                dailyMarketDao.insertAll(entities)
            }
            val percent = ((index + 1) * 100 / total).coerceIn(0, 99)
            _crawlProgress.value = _crawlProgress.value.copy(
                completed = index + 1,
                total = total,
                percent = percent,
                currentTask = "抓取行情 ${stock.stockName}(${index + 1}/$total)"
            )
            saveCrawlState("daily_market", index)
        }
    }

    private suspend fun markSpecialStocks() {
        // 标记新股
        delay(Constants.CRAWL_DELAY_MS)
        val newStocks = east.fetchNewStocks().getOrElse { emptyList() }
        if (newStocks.isNotEmpty()) {
            stockInfoDao.updateFlag(newStocks.map { it.code }, Constants.FLAG_NEW)
        }
    }

    /**
     * 增量刷新实时行情（10分钟调用一次）
     */
    suspend fun refreshRealtimeQuotes(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val holdingCodes = holdingDao.getAllCodes()
            val watchlistCodes = watchlistDao.getAllCodes()
            val allCodes = (holdingCodes + watchlistCodes).distinct()
            if (allCodes.isEmpty()) return@runCatching 0

            // 分批获取（每批50个）
            var updated = 0
            allCodes.chunked(50).forEach { batch ->
                delay(200)
                val quotes = east.fetchRealtimeQuotes(batch).getOrElse { emptyList() }
                for (q in quotes) {
                    holdingDao.updatePrice(q.code, q.currentPrice)
                    watchlistDao.updatePrice(q.code, q.currentPrice)
                    updated++
                }
            }
            updated
        }
    }

    suspend fun fetchKline(code: String, limit: Int = 60): Result<List<DailyBar>> =
        withContext(Dispatchers.IO) {
            val primary = east.fetchDailyKline(code, limit)
            if (primary.isSuccess) return@withContext primary
            val fallback = sina.fetchDailyKline(code, limit)
            fallback.getOrElse { emptyList() }
        }

    // ==================== 持仓管理 ====================

    fun observeHoldings() = holdingDao.observeAll()

    suspend fun getAllHoldings() = holdingDao.getAll()

    suspend fun addHolding(code: String, name: String, shares: Int) {
        val c = StockCodes.normalizeCode(code)
        val stock = stockInfoDao.getStock(c)
        val entity = StockMyHoldingEntity(
            stockCode = c,
            stockName = name.ifBlank { stock?.stockName ?: c },
            industry = stock?.industry ?: "",
            concept = stock?.concept ?: "",
            analystRating = stock?.analystRating ?: "",
            circulatingMarketCap = stock?.circulatingMarketCap ?: "",
            marketCap = stock?.marketCap ?: "",
            currentPrice = stock?.let { parseMarketCapToDouble(it.circulatingMarketCap) } ?: 0.0,
            holdings = shares,
        )
        holdingDao.insert(entity)
    }

    suspend fun removeHolding(code: String) {
        holdingDao.deleteByCode(StockCodes.normalizeCode(code))
    }

    suspend fun clearHoldings() {
        holdingDao.clear()
    }

    // ==================== 关注管理 ====================

    fun observeWatchlist() = watchlistDao.observeAll()

    suspend fun getAllWatchlist() = watchlistDao.getAll()

    suspend fun removeWatch(code: String) {
        watchlistDao.deleteByCode(StockCodes.normalizeCode(code))
    }

    // ==================== 筛选 ====================

    suspend fun getFilterConfig(): FilterConfig {
        val entity = filtersDao.getFilter() ?: return FilterConfig()
        return FilterConfig(
            capFlag = entity.capFlag,
            minCap = entity.minCap,
            maxCap = entity.maxCap,
            ratingMonths = entity.ratingMonths,
            excludedIndustries = entity.excludedIndustry.split(",").filter { it.isNotBlank() }.toSet(),
            coversConcepts = entity.coversConcept.split(",").filter { it.isNotBlank() }.toSet(),
        )
    }

    suspend fun saveFilterConfig(config: FilterConfig) {
        filtersDao.clear()
        filtersDao.insert(
            StockFiltersEntity(
                capFlag = config.capFlag,
                minCap = config.minCap,
                maxCap = config.maxCap,
                ratingMonths = config.ratingMonths,
                excludedIndustry = config.excludedIndustries.joinToString(","),
                coversConcept = config.coversConcepts.joinToString(","),
            )
        )
    }

    /**
     * 执行筛选并更新关注列表
     * 流程：同步持仓进关注 → 删除非持仓关注 → 筛选 → 写入
     */
    suspend fun applyScreen(config: FilterConfig): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. 同步持仓进入关注
            val holdings = holdingDao.getAll()
            for (h in holdings) {
                watchlistDao.insert(
                    StockMyWatchlistEntity(
                        stockCode = h.stockCode,
                        stockName = h.stockName,
                        industry = h.industry,
                        concept = h.concept,
                        analystRating = h.analystRating,
                        circulatingMarketCap = h.circulatingMarketCap,
                        marketCap = h.marketCap,
                        currentPrice = h.currentPrice,
                    )
                )
            }

            // 2. 删除非持仓关注
            val holdingCodes = holdingDao.getAllCodes()
            if (holdingCodes.isEmpty()) {
                watchlistDao.clear()
            } else {
                watchlistDao.deleteExcept(holdingCodes)
            }

            // 3. 从数据库筛选
            val allStocks = stockInfoDao.getNormalStocks()
            var count = 0
            for (stock in allStocks) {
                if (!passesFilter(stock, config)) continue
                watchlistDao.insert(
                    StockMyWatchlistEntity(
                        stockCode = stock.stockCode,
                        stockName = stock.stockName,
                        industry = stock.industry,
                        concept = stock.concept,
                        analystRating = stock.analystRating,
                        circulatingMarketCap = stock.circulatingMarketCap,
                        marketCap = stock.marketCap,
                    )
                )
                count++
            }
            count
        }
    }

    private fun passesFilter(stock: StockInfoEntity, config: FilterConfig): Boolean {
        // 排除ST/退市
        if (StockCodes.shouldExcludeByName(stock.stockName)) return false

        // 市值过滤
        val capStr = if (config.capFlag == 0) stock.circulatingMarketCap else stock.marketCap
        val capValue = parseMarketCapToDouble(capStr)
        if (config.minCap > 0 && capValue > 0 && capValue < config.minCap) return false
        if (config.maxCap > 0 && capValue > 0 && capValue > config.maxCap) return false

        // 行业排除
        if (config.excludedIndustries.isNotEmpty() && stock.industry in config.excludedIndustries) return false

        // 概念题材包含
        if (config.coversConcepts.isNotEmpty()) {
            val stockConcepts = stock.concept.split(";").map { it.trim() }.filter { it.isNotBlank() }.toSet()
            if (stockConcepts.isEmpty() || stockConcepts.intersect(config.coversConcepts).isEmpty()) return false
        }

        return true
    }

    // ==================== 分析 ====================

    /**
     * 分析所有持仓和关注股票
     */
    suspend fun analyzeAll(): Result<List<AnalysisResult>> = withContext(Dispatchers.IO) {
        runCatching {
            val results = mutableListOf<AnalysisResult>()

            // 分析关注列表 -> B信号
            val watchlist = watchlistDao.getAll()
            for (item in watchlist) {
                delay(100)
                val bars = fetchKline(item.stockCode).getOrElse { emptyList() }
                val signal = TechnicalSignals.analyze(bars)
                val resultStr = if (signal.name == "B") Constants.SIGNAL_BUY else Constants.SIGNAL_NONE
                watchlistDao.updateJudgmentResult(item.stockCode, resultStr)
                results.add(AnalysisResult(item.stockCode, item.stockName, resultStr))
            }

            // 分析持仓列表 -> S信号
            val holdings = holdingDao.getAll()
            for (item in holdings) {
                delay(100)
                val bars = fetchKline(item.stockCode).getOrElse { emptyList() }
                val signal = TechnicalSignals.analyze(bars)
                val resultStr = if (signal.name == "S") Constants.SIGNAL_SELL else Constants.SIGNAL_NONE
                holdingDao.updateJudgmentResult(item.stockCode, resultStr)
                // 更新或添加到结果
                val existing = results.find { it.stockCode == item.stockCode }
                if (existing != null) {
                    results[results.indexOf(existing)] = existing.copy(signal = resultStr)
                } else {
                    results.add(AnalysisResult(item.stockCode, item.stockName, resultStr))
                }
            }

            results
        }
    }

    // ==================== 断点续传 ====================

    private fun saveCrawlState(task: String, index: Int) {
        crawlPrefs.edit()
            .putString("current_task", task)
            .putInt("last_index", index)
            .apply()
    }

    private fun clearCrawlState() {
        crawlPrefs.edit().clear().apply()
    }

    fun hasCrawlState(): Boolean {
        return crawlPrefs.contains("current_task")
    }

    // ==================== 工具方法 ====================

    private fun parseMarketCapToDouble(capStr: String): Double {
        if (capStr.isBlank()) return 0.0
        return capStr.replace("亿", "").replace(",", "").toDoubleOrNull() ?: 0.0
    }
}
