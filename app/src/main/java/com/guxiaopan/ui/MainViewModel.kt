package com.guxiaopan.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guxiaopan.GuXiaoPanApp
import com.guxiaopan.common.Constants
import com.guxiaopan.data.StockRepository
import com.guxiaopan.data.model.CrawlProgress
import com.guxiaopan.data.model.CrawlStatus
import com.guxiaopan.data.model.FilterConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repository: StockRepository by lazy {
        StockRepository((app as GuXiaoPanApp).database, app)
    }

    // ==================== 用户状态 ====================

    val user = repository.observeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    fun login(phone: String) {
        viewModelScope.launch {
            val user = repository.login(phone)
            _loginSuccess.value = true
        }
    }

    fun tryAutoLogin() {
        viewModelScope.launch {
            val existing = repository.getUser()
            if (existing != null) {
                _loginSuccess.value = true
            }
        }
    }

    fun submitAuthCode(authCode: String) {
        viewModelScope.launch {
            val currentUser = repository.getUser() ?: return@launch
            // 简单解析授权码（实际生产环境使用RSA验证）
            try {
                val decoded = android.util.Base64.decode(authCode, android.util.Base64.DEFAULT)
                val json = String(decoded, Charsets.UTF_8)
                // 解析days和expiry
                val daysMatch = Regex(""""days"\s*:\s*(\d+)""").find(json)
                val expiryMatch = Regex(""""expiry"\s*:\s*(\d+)""").find(json)
                val days = daysMatch?.groupValues?.get(1)?.toIntOrNull() ?: 30
                val expiry = expiryMatch?.groupValues?.get(1)?.toLongOrNull()
                    ?: (System.currentTimeMillis() + days * 24 * 60 * 60 * 1000L)
                repository.updateAuth(currentUser.phone, authCode, days, expiry)
                _toast.tryEmit("授权成功，已激活 $days 天")
            } catch (e: Exception) {
                _toast.tryEmit("授权码无效，请检查后重试")
            }
        }
    }

    // ==================== 数据抓取 ====================

    val crawlProgress = repository.crawlProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CrawlProgress.idle())

    fun startCrawl() {
        viewModelScope.launch {
            repository.startFullCrawl()
        }
    }

    fun refreshRealtime() {
        viewModelScope.launch {
            repository.refreshRealtimeQuotes()
        }
    }

    // ==================== 持仓管理 ====================

    val holdings = repository.observeHoldings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addHolding(code: String, name: String, shares: Int) {
        viewModelScope.launch {
            repository.addHolding(code, name, shares)
            _toast.tryEmit("已添加持仓")
        }
    }

    fun removeHolding(code: String) {
        viewModelScope.launch {
            repository.removeHolding(code)
            _toast.tryEmit("已删除持仓")
        }
    }

    fun clearHoldings() {
        viewModelScope.launch {
            repository.clearHoldings()
            _toast.tryEmit("已清空持仓")
        }
    }

    // ==================== 关注管理 ====================

    val watchlist = repository.observeWatchlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun removeWatch(code: String) {
        viewModelScope.launch {
            repository.removeWatch(code)
            _toast.tryEmit("已移除关注")
        }
    }

    // ==================== 筛选 ====================

    private val _filterConfig = MutableStateFlow(FilterConfig())
    val filterConfig: StateFlow<FilterConfig> = _filterConfig.asStateFlow()

    init {
        viewModelScope.launch {
            _filterConfig.value = repository.getFilterConfig()
        }
    }

    fun updateFilterConfig(config: FilterConfig) {
        _filterConfig.value = config
        viewModelScope.launch {
            repository.saveFilterConfig(config)
        }
    }

    fun applyScreen() {
        viewModelScope.launch {
            _busy.value = true
            val result = repository.applyScreen(_filterConfig.value)
            _busy.value = false
            if (result.isSuccess) {
                _toast.tryEmit("筛选完成，写入 ${result.getOrNull()} 只标的")
            } else {
                _toast.tryEmit("筛选失败：${result.exceptionOrNull()?.message}")
            }
        }
    }

    // ==================== 分析 ====================

    fun analyzeAll() {
        viewModelScope.launch {
            _busy.value = true
            val result = repository.analyzeAll()
            _busy.value = false
            if (result.isSuccess) {
                val buyCount = result.getOrNull()?.count { it.signal == Constants.SIGNAL_BUY } ?: 0
                val sellCount = result.getOrNull()?.count { it.signal == Constants.SIGNAL_SELL } ?: 0
                _toast.tryEmit("分析完成：B信号 $buyCount 只，S信号 $sellCount 只")
            } else {
                _toast.tryEmit("分析失败：${result.exceptionOrNull()?.message}")
            }
        }
    }

    // ==================== 通用状态 ====================

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _toast = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toast = _toast.asSharedFlow()
}
