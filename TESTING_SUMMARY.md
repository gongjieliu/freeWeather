# 天气 App 云手机测试与问题修复总结

## 测试环境

- **云手机**: 阿里云无影 (8.210.52.196:100)
- **操作系统**: Android 14
- **ADB 工具**: /opt/android-sdk/platform-tools/adb

## 连接方式

1. 使用阿里云无影云手机的「一键ADB」功能
2. 需要配置密钥对认证：
   - 在阿里云控制台创建密钥对
   - 绑定到云手机实例
   - 下载私钥文件 `adbkey`
   - 复制到 `~/.android/adbkey` 并设置权限 `chmod 600 ~/.android/adbkey`
3. 连接命令：`adb connect 8.210.52.196:100`

## 发现的问题与修复

### 1. API 端点错误 (HTTP 404)

**问题**: 城市搜索 API 返回 404

**原因**: QWeather API 已从 v7 版本升级，旧的端点路径已变更

**修复**:
- 城市搜索: `/v7/search` → `/geo/v2/city/lookup`
- 天气查询: `/v7/weather/now` → `/v7/weather/now` (保持)
- 天气预报: `/v7/weather/3d` → `/v7/weather/3d` (保持)

### 2. API Host 错误 (HTTP 403)

**问题**: 天气 API 返回 403 Forbidden

**原因**: QWeather 现在需要使用专属的 API Host，而不是通用的 `api.qweather.com`

**修复**:
- 将 BASE_URL 改为: `https://nf4up53xqj.re.qweatherapi.com/`

### 3. API Key 格式错误

**问题**: 认证失败 (HTTP 401)

**原因**: 用户输入的 API Key 包含了域名后缀

**修复**:
- 只使用 Key 本身: `aac31a9b3a4249bc8a18b5da33ca9b40`
- 通过 Header 传递: `X-QW-Api-Key`

### 4. DTO 字段映射错误 (JSON 解析失败)

**问题**: `Required value 'condCode' missing at $.now`

**原因**: API v7 响应字段名与 DTO 定义不匹配

**修复** - NowWeather DTO:
```kotlin
// 旧字段 (不匹配)
condCode, condTxt, uvIndex

// 新字段 (正确)
icon (对应 condCode)
text (对应 condTxt)
```

### 5. 天气预报字段映射错误

**问题**: `Required value 'now' missing at $` (forecast API 返回无 now 字段)

**原因**: 天气预报 API 和实时天气 API 返回结构不同

**修复**:
- WeatherResponse 中 now 字段改为可选: `NowWeather?`
- forecast 字段改为 `daily`
- ForecastDay 字段调整:
  - `condCodeD` → `iconDay`
  - `condTxtD` → `textDay`
  - `condCodeN` → `iconNight`
  - `condTxtN` → `textNight`

### 6. 城市名称未显示

**问题**: 首页显示天气但城市名为空

**原因**: 天气响应中不包含城市名称，需要从城市列表中获取

**修复**:
- 在 HomeViewModel 中，使用已保存的城市名称覆盖 API 返回的天气描述

### 7. 数据库表未创建

**问题**: `no such table: cities`

**原因**: Room 数据库未正确创建表

**修复**:
- 数据库名从 `weather_database` 改为 `weather_db_v2`
- 添加 `fallbackToDestructiveMigration()` 配置
- 重新安装应用以创建新数据库

## 关键代码文件

- `WeatherApiService.kt` - API 端点定义
- `AppModule.kt` - DI 配置 (BASE_URL, API Key 拦截器)
- `WeatherDto.kt` - 数据模型 (字段映射)
- `RepositoryImpl.kt` - 数据仓库 (API 调用逻辑)
- `HomeViewModel.kt` - 首页逻辑 (城市名称处理)

## 测试命令

```bash
# 查看日志
adb logcat -d | grep -E "WeatherRepo|HomeViewModel" | tail -30

# 安装 APK
adb -s 8.210.52.196:100 install app/build/outputs/apk/debug/app-debug.apk

# 启动应用
adb shell am start -n com.weather.app/.MainActivity

# 查看数据库
adb shell "run-as com.weather.app sh -c 'sqlite3 /data/data/com.weather.app/databases/weather_db_v2.db \"SELECT * FROM cities;\"'"
```

## 后续优化建议

1. 简化 API Key 配置流程
2. 增加错误提示信息
3. 优化网络请求的错误处理
4. 考虑使用官方 SDK
