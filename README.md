# 股小判 - 散户炒股精灵

## 项目概述

股小判是一款面向A股散户投资者的股票分析辅助工具（Android应用），通过自动抓取东方财富网的股票行情数据，结合MACD和SKDJ技术指标分析，帮助用户识别潜在买卖时机。

## 技术栈

- **开发语言**: Kotlin
- **最低SDK**: Android 7.0 (API 24)
- **架构模式**: MVVM + Repository
- **UI框架**: Jetpack Compose
- **数据库**: Room (SQLite)
- **网络请求**: OkHttp
- **数据解析**: org.json

## 核心功能

### 1. 用户管理
- 手机号登录
- 30天免费体验
- RSA授权码验证
- 授权到期提醒

### 2. 数据抓取
- 沪深京A股基本信息
- 日K线数据（60日）
- 实时行情（10分钟刷新）
- 概念题材数据
- 机构评级数据
- 增量更新 + 断点续传

### 3. 股票分析
- MACD指标（12/26/9）
- SKDJ指标（9/3/3）
- 金叉/死叉判定
- B/S信号生成

### 4. 持仓管理
- 添加/删除持仓
- 持仓列表展示
- S信号标记（黄色）

### 5. 选股筛选
- 市值范围筛选
- 行业排除
- 概念题材筛选
- 关注列表更新
- B信号标记（红色）

## 项目结构

```
com.guxiaopan/
├── common/              # 常量、异常定义
│   ├── Constants.kt
│   └── Exceptions.kt
├── data/                # 数据层
│   ├── AppDatabase.kt
│   ├── StockRepository.kt
│   ├── ScheduleManager.kt
│   ├── local/           # 本地数据
│   │   ├── entity/      # Room实体
│   │   └── dao/         # Room DAO
│   ├── remote/          # 远程API
│   │   ├── EastMoneyClient.kt
│   │   ├── SinaClient.kt
│   │   └── DailyBar.kt
│   └── model/           # 数据模型
├── domain/              # 业务逻辑
│   ├── TechnicalSignals.kt
│   └── TradeSignal.kt
├── ui/                  # UI层
│   ├── MainActivity.kt
│   ├── LoginActivity.kt
│   ├── MainScreen.kt
│   ├── MainViewModel.kt
│   └── theme/
└── util/                # 工具类
    ├── StockCodes.kt
    ├── CryptoUtils.kt
    ├── Logger.kt
    └── Result.kt
```

## 技术指标说明

### MACD (12, 26, 9)
- **快线周期**: 12
- **慢线周期**: 26
- **信号线周期**: 9

### SKDJ (9, 3, 3)
- **RSV周期**: 9
- **K线平滑**: 3
- **D线平滑**: 3

### 信号判定
- **B信号（买）**: MACD即将金叉 + SKDJ已金叉
- **S信号（卖）**: MACD已死叉 + SKDJ即将死叉

## 构建说明

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 35

### 构建步骤
1. 克隆项目到本地
2. 使用Android Studio打开项目
3. 等待Gradle同步完成
4. 连接设备或启动模拟器
5. 点击运行按钮

### 发布构建
```bash
./gradlew assembleRelease
```

## 测试

### 单元测试
```bash
./gradlew test
```

测试覆盖:
- MACD/SKDJ算法
- 股票代码工具类

## 注意事项

1. **API限制**: 东方财富API为非官方公开接口，频繁请求可能触发限制
2. **数据准确性**: 技术指标仅供参考，不构成投资建议
3. **授权码**: 生产环境需替换CryptoUtils中的占位公钥
4. **概念题材**: 部分股票可能无概念数据

## 许可证

仅供学习研究使用

## 联系方式

- 微信/电话: 18674827052