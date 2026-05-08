package com.guxiaopan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guxiaopan.common.Constants
import com.guxiaopan.data.local.entity.StockMyHoldingEntity
import com.guxiaopan.data.local.entity.StockMyWatchlistEntity
import com.guxiaopan.data.model.CrawlStatus
import kotlinx.coroutines.flow.collectLatest

private val BuyBg = Color(0xFFFFCDD2)
private val SellBg = Color(0xFFFFF9C4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val vm: MainViewModel = viewModel()
    val holdings by vm.holdings.collectAsStateWithLifecycle()
    val watchlist by vm.watchlist.collectAsStateWithLifecycle()
    val crawlProgress by vm.crawlProgress.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val filterConfig by vm.filterConfig.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.toast.collectLatest { msg -> snackbar.showSnackbar(msg) }
    }

    // 对话框状态
    var showAddHolding by remember { mutableStateOf(false) }
    var showFilterSettings by remember { mutableStateOf(false) }

    val isCrawling = crawlProgress.status == CrawlStatus.CRAWLING
    val crawlComplete = crawlProgress.status == CrawlStatus.COMPLETED

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("股小判") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(onClick = { vm.refreshRealtime() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // ========== 1. 数据更新进度条 ==========
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                if (isCrawling) {
                    LinearProgressIndicator(
                        progress = { crawlProgress.percent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${crawlProgress.currentTask} ${crawlProgress.percent}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (crawlComplete) {
                    Text(
                        text = "数据已就绪",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { vm.startCrawl() },
                        enabled = !isCrawling && !busy,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (crawlComplete) "重新抓取数据" else "开始抓取数据")
                    }
                }
            }

            // ========== 2. 我的持仓 ==========
            SectionHeader(
                title = "我的持仓",
                actions = {
                    TextButton(onClick = { vm.clearHoldings() }, enabled = holdings.isNotEmpty() && !busy) {
                        Text("清空", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { showAddHolding = true }, enabled = !busy) {
                        Text("添加")
                    }
                }
            )
            if (holdings.isEmpty()) {
                Text(
                    "暂无持仓，点击添加",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(holdings, key = { it.stockCode }) { item ->
                    HoldingItem(item = item, onDelete = { vm.removeHolding(it) })
                }
            }

            // ========== 3. 选股条件 ==========
            SectionHeader(
                title = "选股条件",
                actions = {
                    TextButton(onClick = { showFilterSettings = true }, enabled = !busy) {
                        Text("配置")
                    }
                }
            )
            FilterSummary(config = filterConfig, modifier = Modifier.padding(horizontal = 16.dp))

            // ========== 4. 更新我的关注 ==========
            Button(
                onClick = { vm.applyScreen() },
                enabled = crawlComplete && !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Text("更新我的关注")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ========== 5. 我的关注 ==========
            SectionHeader(title = "我的关注")
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(watchlist, key = { it.stockCode }) { item ->
                    WatchlistItem(item = item, onRemove = { vm.removeWatch(it) })
                }
            }

            // ========== 6. 小判一下 ==========
            Button(
                onClick = { vm.analyzeAll() },
                enabled = crawlComplete && !busy && (holdings.isNotEmpty() || watchlist.isNotEmpty()),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    "小判一下",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    // ========== 对话框 ==========

    if (showAddHolding) {
        AddHoldingDialog(
            onDismiss = { showAddHolding = false },
            onConfirm = { code, name, shares ->
                vm.addHolding(code, name, shares)
                showAddHolding = false
            }
        )
    }

    if (showFilterSettings) {
        FilterSettingsDialog(
            currentConfig = filterConfig,
            onDismiss = { showFilterSettings = false },
            onConfirm = { config ->
                vm.updateFilterConfig(config)
                showFilterSettings = false
            }
        )
    }
}

// ==================== 组件 ====================

@Composable
private fun SectionHeader(
    title: String,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Row { actions() }
    }
}

@Composable
private fun HoldingItem(
    item: StockMyHoldingEntity,
    onDelete: (String) -> Unit,
) {
    val bg = if (item.judgmentResult == Constants.SIGNAL_SELL) SellBg else MaterialTheme.colorScheme.surface
    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${item.stockCode} ${item.stockName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (item.judgmentResult == Constants.SIGNAL_SELL) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "S",
                            color = Color(0xFFF57F17),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
                Text(
                    "持仓 ${item.holdings} 股",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onDelete(item.stockCode) }) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun WatchlistItem(
    item: StockMyWatchlistEntity,
    onRemove: (String) -> Unit,
) {
    val bg = if (item.judgmentResult == Constants.SIGNAL_BUY) BuyBg else MaterialTheme.colorScheme.surface
    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${item.stockCode} ${item.stockName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (item.judgmentResult == Constants.SIGNAL_BUY) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "B",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
                if (item.industry.isNotBlank()) {
                    Text(
                        item.industry,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = { onRemove(item.stockCode) }) {
                Icon(Icons.Default.Delete, contentDescription = "移除", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FilterSummary(config: com.guxiaopan.data.model.FilterConfig, modifier: Modifier = Modifier) {
    val capType = if (config.capFlag == 0) "流通市值" else "总市值"
    val capRange = when {
        config.minCap > 0 && config.maxCap > 0 -> "$capType: ${config.minCap}-${config.maxCap}亿"
        config.minCap > 0 -> "$capType: ≥${config.minCap}亿"
        config.maxCap > 0 -> "$capType: ≤${config.maxCap}亿"
        else -> "$capType: 不限"
    }
    val excluded = if (config.excludedIndustries.isNotEmpty()) {
        "排除: ${config.excludedIndustries.joinToString("、")}"
    } else ""
    val concepts = if (config.coversConcepts.isNotEmpty()) {
        "概念: ${config.coversConcepts.joinToString("、")}"
    } else ""

    Text(
        text = listOf(capRange, excluded, concepts).filter { it.isNotBlank() }.joinToString(" | "),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun AddHoldingDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String, name: String, shares: Int) -> Unit,
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var shares by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加持仓") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.filter { ch -> ch.isDigit() } },
                    label = { Text("股票代码") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("股票名称（可空）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = shares,
                    onValueChange = { shares = it.filter { ch -> ch.isDigit() } },
                    label = { Text("持仓数量") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val s = shares.toIntOrNull() ?: 0
                    if (code.length == 6 && s > 0) {
                        onConfirm(code, name, s)
                    }
                },
                enabled = code.length == 6 && shares.toIntOrNull() ?: 0 > 0,
            ) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSettingsDialog(
    currentConfig: com.guxiaopan.data.model.FilterConfig,
    onDismiss: () -> Unit,
    onConfirm: (com.guxiaopan.data.model.FilterConfig) -> Unit,
) {
    var capFlag by remember { mutableStateOf(currentConfig.capFlag) }
    var minCap by remember { mutableStateOf(if (currentConfig.minCap > 0) currentConfig.minCap.toString() else "") }
    var maxCap by remember { mutableStateOf(if (currentConfig.maxCap > 0) currentConfig.maxCap.toString() else "") }
    var ratingMonths by remember { mutableStateOf(currentConfig.ratingMonths.toString()) }
    val excludedMap = remember { mutableStateOf(currentConfig.excludedIndustries.associateWith { true }.toMutableMap()) }
    var manualIndustry by remember { mutableStateOf("") }

    // 确保默认排除行业存在
    Constants.DEFAULT_EXCLUDED_INDUSTRIES.forEach {
        if (!excludedMap.value.containsKey(it)) excludedMap.value[it] = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选股条件配置") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 市值类型
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = { capFlag = 0 },
                        colors = if (capFlag == 0) ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else ButtonDefaults.filledTonalButtonColors(),
                    ) { Text("流通市值") }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = { capFlag = 1 },
                        colors = if (capFlag == 1) ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else ButtonDefaults.filledTonalButtonColors(),
                    ) { Text("总市值") }
                }

                // 市值范围
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = minCap,
                        onValueChange = { minCap = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("最小(亿)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = maxCap,
                        onValueChange = { maxCap = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("最大(亿)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }

                // 评级统计月数
                OutlinedTextField(
                    value = ratingMonths,
                    onValueChange = { ratingMonths = it.filter { ch -> ch.isDigit() } },
                    label = { Text("评级统计月数") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // 排除行业
                Text("排除行业：", style = MaterialTheme.typography.labelMedium)
                Constants.DEFAULT_EXCLUDED_INDUSTRIES.forEach { industry ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(
                            checked = excludedMap.value[industry] == true,
                            onCheckedChange = { excludedMap.value[industry] = it },
                        )
                        Text(industry, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // 手动添加排除行业
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = manualIndustry,
                        onValueChange = { manualIndustry = it },
                        label = { Text("添加排除行业") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(
                        onClick = {
                            val v = manualIndustry.trim()
                            if (v.isNotBlank()) {
                                excludedMap.value[v] = true
                                manualIndustry = ""
                            }
                        },
                    ) { Text("加入") }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        com.guxiaopan.data.model.FilterConfig(
                            capFlag = capFlag,
                            minCap = minCap.toDoubleOrNull() ?: 0.0,
                            maxCap = maxCap.toDoubleOrNull() ?: 0.0,
                            ratingMonths = ratingMonths.toIntOrNull() ?: 3,
                            excludedIndustries = excludedMap.value.filterValues { it }.keys,
                        )
                    )
                },
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
