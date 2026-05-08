package com.guxiaopan.util

import org.junit.Test
import org.junit.Assert.*

class StockCodesTest {

    @Test
    fun `test normalizeCode pads to 6 digits`() {
        assertEquals("000001", StockCodes.normalizeCode("1"))
        assertEquals("000001", StockCodes.normalizeCode("000001"))
        assertEquals("600000", StockCodes.normalizeCode("600000"))
    }

    @Test
    fun `test isShanghai detects Shanghai stocks`() {
        assertTrue(StockCodes.isShanghai("600000"))
        assertTrue(StockCodes.isShanghai("688001"))
        assertTrue(StockCodes.isShanghai("689001"))
        assertFalse(StockCodes.isShanghai("000001"))
        assertFalse(StockCodes.isShanghai("300001"))
    }

    @Test
    fun `test isShenzhen detects Shenzhen stocks`() {
        assertTrue(StockCodes.isShenzhen("000001"))
        assertTrue(StockCodes.isShenzhen("300001"))
        assertFalse(StockCodes.isShenzhen("600000"))
    }

    @Test
    fun `test isStarMarket detects STAR Market`() {
        assertTrue(StockCodes.isStarMarket("688001"))
        assertTrue(StockCodes.isStarMarket("689001"))
        assertFalse(StockCodes.isStarMarket("600000"))
    }

    @Test
    fun `test isChiNext detects ChiNext`() {
        assertTrue(StockCodes.isChiNext("300001"))
        assertFalse(StockCodes.isChiNext("000001"))
    }

    @Test
    fun `test shouldExcludeByName filters ST stocks`() {
        assertTrue(StockCodes.shouldExcludeByName("ST股票"))
        assertTrue(StockCodes.shouldExcludeByName("*ST股票"))
        assertTrue(StockCodes.shouldExcludeByName("退市股票"))
        assertFalse(StockCodes.shouldExcludeByName("正常股票"))
    }

    @Test
    fun `test secIdFor generates correct secid`() {
        assertEquals("1.600000", StockCodes.secIdFor("600000"))
        assertEquals("0.000001", StockCodes.secIdFor("000001"))
        assertEquals("0.300001", StockCodes.secIdFor("300001"))
    }
}
