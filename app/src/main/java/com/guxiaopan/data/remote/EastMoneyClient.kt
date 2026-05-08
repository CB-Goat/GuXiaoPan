package com.guxiaopan.data.remote

import com.guxiaopan.util.StockCodes
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class EastMoneyClient(
    private val client: OkHttpClient = defaultClient(),
) {
    fun fetchDailyKline(code: String, limit: Int = 120): Result<List<DailyBar>> = runCatching {
        val secid = StockCodes.secIdFor(code)
        val url = "https://push2his.eastmoney.com/api/qt/stock/kline/get?" +
            "secid=$secid&ut=fa5fd1943c7b386f172d6893dbfba10b&" +
            "fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13&" +
            "fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61,f62,f63,f64,f65&" +
            "klt=101&fqt=1&end=20500101&lmt=$limit"
        val json = getJson(url)
        val data = json.optJSONObject("data") ?: error("东财K线无 data")
        val arr = data.optJSONArray("klines") ?: error("东财K线无 klines")
        buildList {
            for (i in 0 until arr.length()) {
                val line = arr.getString(i)
                val p = line.split(',')
                if (p.size >= 11) {
                    add(
                        DailyBar(
                            date = p[0],
                            open = p[1].toDoubleOrNull() ?: 0.0,
                            close = p[2].toDoubleOrNull() ?: 0.0,
                            high = p[3].toDoubleOrNull() ?: 0.0,
                            low = p[4].toDoubleOrNull() ?: 0.0,
                            volume = p[5].toDoubleOrNull() ?: 0.0,
                            amount = p[6].toDoubleOrNull() ?: 0.0,
                            changePercent = p[8].toDoubleOrNull() ?: 0.0,
                            turnoverRate = if (p.size > 10) p[10].toDoubleOrNull() ?: 0.0 else 0.0,
                        )
                    )
                }
            }
        }
    }

    fun fetchStockPage(page: Int, pageSize: Int = 500): Result<List<StockListRow>> = runCatching {
        val fs = "m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23,m:0+t:81+s:2048"
        val fields = "f12,f14,f20,f21,f100,f3,f2,f18,f17,f15,f16,f8,f10"
        val url = "https://push2.eastmoney.com/api/qt/clist/get?" +
            "pn=$page&pz=$pageSize&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&" +
            "fltt=2&invt=2&fid=f3&fs=$fs&fields=$fields"
        val json = getJson(url)
        val data = json.optJSONObject("data") ?: return@runCatching emptyList()
        val diff = data.optJSONArray("diff") ?: return@runCatching emptyList()
        buildList {
            for (i in 0 until diff.length()) {
                val o = diff.getJSONObject(i)
                val code = o.optString("f12").trim().padStart(6, '0')
                val name = o.optString("f14")
                if (code.isBlank() || name.isBlank()) continue
                add(
                    StockListRow(
                        code = code,
                        name = name,
                        industry = o.optString("f100"),
                        totalMarketCapYi = yuanToYi(o.optDouble("f20", Double.NaN)),
                        circulatingMarketCapYi = yuanToYi(o.optDouble("f21", Double.NaN)),
                        prevClose = o.optDouble("f18", Double.NaN).takeIf { !it.isNaN() } ?: 0.0,
                        currentPrice = o.optDouble("f2", Double.NaN).takeIf { !it.isNaN() } ?: 0.0,
                        changePercent = o.optDouble("f3", Double.NaN).takeIf { !it.isNaN() } ?: 0.0,
                        turnoverRate = o.optDouble("f8", Double.NaN).takeIf { !it.isNaN() } ?: 0.0,
                        volumeRatio = o.optDouble("f10", Double.NaN).takeIf { !it.isNaN() } ?: 0.0,
                    )
                )
            }
        }
    }

    fun fetchNewStocks(page: Int = 1, pageSize: Int = 500): Result<List<StockListRow>> = runCatching {
        val fs = "m:0+t:6+f:!2,m:1+t:2+f:!2,m:0+t:80+f:!2,m:1+t:23+f:!2"
        val fields = "f12,f14,f100"
        val url = "https://push2.eastmoney.com/api/qt/clist/get?" +
            "pn=$page&pz=$pageSize&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&" +
            "fltt=2&invt=2&fid=f3&fs=$fs&fields=$fields"
        val json = getJson(url)
        val data = json.optJSONObject("data") ?: return@runCatching emptyList()
        val diff = data.optJSONArray("diff") ?: return@runCatching emptyList()
        buildList {
            for (i in 0 until diff.length()) {
                val o = diff.getJSONObject(i)
                val code = o.optString("f12").trim().padStart(6, '0')
                val name = o.optString("f14")
                if (code.isNotBlank() && name.isNotBlank()) {
                    add(StockListRow(code = code, name = name, industry = o.optString("f100")))
                }
            }
        }
    }

    fun fetchRealtimeQuotes(codes: List<String>): Result<List<RealtimeQuote>> = runCatching {
        if (codes.isEmpty()) return@runCatching emptyList()
        val secids = codes.map { StockCodes.secIdFor(it) }.joinToString(",")
        val fields = "f12,f14,f2,f3,f15,f16,f17,f18,f8,f10"
        val url = "https://push2.eastmoney.com/api/qt/ulist.np/get?" +
            "fltt=2&fields=$fields&secids=$secids"
        val json = getJson(url)
        val data = json.optJSONObject("data") ?: return@runCatching emptyList()
        val diff = data.optJSONArray("diff") ?: return@runCatching emptyList()
        buildList {
            for (i in 0 until diff.length()) {
                val o = diff.getJSONObject(i)
                val code = o.optString("f12", "").trim().padStart(6, '0')
                if (code.isBlank()) continue
                add(
                    RealtimeQuote(
                        code = code,
                        name = o.optString("f14"),
                        currentPrice = o.optDouble("f2", 0.0),
                        changePercent = o.optDouble("f3", 0.0),
                        high = o.optDouble("f15", 0.0),
                        low = o.optDouble("f16", 0.0),
                        open = o.optDouble("f17", 0.0),
                        prevClose = o.optDouble("f18", 0.0),
                        turnoverRate = o.optDouble("f8", 0.0),
                        volumeRatio = o.optDouble("f10", 0.0),
                    )
                )
            }
        }
    }

    fun fetchStockConcepts(code: String): Result<StockConceptData> = runCatching {
        val marketPrefix = when {
            StockCodes.isShanghai(code) -> "SH"
            StockCodes.isBSE(code) -> "BJ"
            else -> "SZ"
        }
        val fullCode = "$marketPrefix${StockCodes.normalizeCode(code)}"
        val url = "http://emweb.securities.eastmoney.com/PC_HSF10/CoreConception/PageAjax?code=$fullCode"
        val json = getJson(url)
        val result = json.optJSONObject("result") ?: error("概念数据无result")
        val data = result.optJSONObject("data") ?: error("概念数据无data")
        val gnArray = data.optJSONArray("gn") ?: org.json.JSONArray()
        val concepts = mutableListOf<String>()
        for (i in 0 until gnArray.length()) {
            val obj = gnArray.getJSONObject(i)
            val name = obj.optString("name", "")
            if (name.isNotBlank()) concepts.add(name)
        }
        val hyArray = data.optJSONArray("hy") ?: org.json.JSONArray()
        val industries = mutableListOf<String>()
        for (i in 0 until hyArray.length()) {
            val obj = hyArray.getJSONObject(i)
            val name = obj.optString("name", "")
            if (name.isNotBlank()) industries.add(name)
        }
        StockConceptData(code = code, concepts = concepts, industries = industries)
    }

    fun fetchAnalystRating(code: String, months: Int = 6): Result<String> = runCatching {
        val normalizedCode = StockCodes.normalizeCode(code)
        val url = "https://datacenter-web.eastmoney.com/api/data/v1/get?" +
            "sortColumns=RATING_ORG_NUM&sortTypes=-1&pageSize=500&pageNumber=1&" +
            "reportName=RPT_CUSTOM_STOCK_RATING&columns=ALL&" +
            "filter=(SECURITY_CODE=%22$normalizedCode%22)"
        val json = getJson(url)
        val result = json.optJSONObject("result") ?: return@runCatching ""
        val dataArr = result.optJSONArray("data") ?: return@runCatching ""
        if (dataArr.length() == 0) return@runCatching ""
        val item = dataArr.getJSONObject(0)
        val buy3m = item.optInt("BUY_3M", 0)
        val add3m = item.optInt("ADD_3M", 0)
        val buy6m = item.optInt("BUY_6M", 0)
        val add6m = item.optInt("ADD_6M", 0)
        buildString {
            append("3M-买入$buy3m，增持$add3m")
            if (months >= 6) append("；6M-买入$buy6m，增持$add6m")
        }
    }

    private fun yuanToYi(yuan: Double): Double =
        if (yuan.isNaN() || yuan <= 0) 0.0 else yuan / 100_000_000.0

    private fun getJson(url: String): JSONObject {
        val req = Request.Builder().url(url).header("User-Agent", UA).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            val body = resp.body?.string().orEmpty()
            return JSONObject(body)
        }
    }

    companion object {
        private const val UA = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36"
        fun defaultClient(): OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
    }
}

data class StockListRow(val code: String, val name: String, val industry: String = "", val totalMarketCapYi: Double = 0.0, val circulatingMarketCapYi: Double = 0.0, val prevClose: Double = 0.0, val currentPrice: Double = 0.0, val changePercent: Double = 0.0, val turnoverRate: Double = 0.0, val volumeRatio: Double = 0.0)
data class RealtimeQuote(val code: String, val name: String = "", val currentPrice: Double = 0.0, val changePercent: Double = 0.0, val high: Double = 0.0, val low: Double = 0.0, val open: Double = 0.0, val prevClose: Double = 0.0, val turnoverRate: Double = 0.0, val volumeRatio: Double = 0.0)
data class StockConceptData(val code: String, val concepts: List<String> = emptyList(), val industries: List<String> = emptyList())