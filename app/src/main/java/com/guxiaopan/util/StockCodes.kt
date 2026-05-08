package com.guxiaopan.util

/**
 * A股代码工具类
 */
object StockCodes {

    /** A股东财secid：上证 1.xxxxxx，深证 0.xxxxxx */
    fun secIdFor(code: String): String {
        val c = code.trim().padStart(6, '0')
        return if (c.startsWith("6") || c.startsWith("688") || c.startsWith("689")) {
            "1.$c"
        } else {
            "0.$c"
        }
    }

    /** 代码归一化为6位 */
    fun normalizeCode(raw: String): String = raw.trim().padStart(6, '0')

    /** 判断是否为上证代码 */
    fun isShanghai(code: String): Boolean {
        val c = normalizeCode(code)
        return c.startsWith("6")
    }

    /** 判断是否为深证代码 */
    fun isShenzhen(code: String): Boolean {
        val c = normalizeCode(code)
        return c.startsWith("0") || c.startsWith("3")
    }

    /** 判断是否为科创板 */
    fun isStarMarket(code: String): Boolean {
        val c = normalizeCode(code)
        return c.startsWith("688")
    }

    /** 判断是否为创业板 */
    fun isChiNext(code: String): Boolean {
        val c = normalizeCode(code)
        return c.startsWith("3")
    }

    /** 判断是否为北交所 */
    fun isBSE(code: String): Boolean {
        val c = normalizeCode(code)
        return c.startsWith("8") || c.startsWith("4")
    }

    /** 排除ST、退市名称 */
    fun shouldExcludeByName(name: String): Boolean {
        val n = name.uppercase()
        return n.contains("ST") || n.contains("退")
    }
}