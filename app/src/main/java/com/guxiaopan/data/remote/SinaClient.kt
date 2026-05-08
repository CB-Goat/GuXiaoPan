package com.guxiaopan.data.remote

import com.guxiaopan.util.StockCodes
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

/**
 * 新浪K线备用接口（日线 scale=240）
 * 作为东财失败时的降级方案
 */
class SinaClient(
    private val client: OkHttpClient = EastMoneyClient.defaultClient(),
) {
    fun fetchDailyKline(code: String, limit: Int = 120): Result<List<DailyBar>> = runCatching {
        val sym = sinaSymbol(code)
        val url = "https://quotes.sina.cn/cn/api/json_v2.php/CN_MarketDataService.getKLineData?" +
            "symbol=$sym&scale=240&datalen=$limit"
        val req = Request.Builder().url(url).header("User-Agent", UA).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            val body = resp.body?.string().orEmpty().trim()
            if (body.isEmpty() || body[0] != '[') error("新浪K线非 JSON 数组")
            val arr = JSONArray(body)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        DailyBar(
                            date = o.optString("day", o.optString("date")),
                            open = o.optDouble("open", Double.NaN),
                            high = o.optDouble("high", Double.NaN),
                            low = o.optDouble("low", Double.NaN),
                            close = o.optDouble("close", Double.NaN),
                            volume = o.optDouble("volume", 0.0),
                            amount = 0.0,
                        )
                    )
                }
            }.filter { it.close.isFinite() && it.open.isFinite() }
        }
    }

    private fun sinaSymbol(code: String): String {
        val c = StockCodes.normalizeCode(code)
        val prefix = if (StockCodes.isShanghai(c)) "sh" else "sz"
        return "$prefix$c"
    }

    companion object {
        private const val UA =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
            "Chrome/120.0.0.0 Mobile Safari/537.36"
    }
}
