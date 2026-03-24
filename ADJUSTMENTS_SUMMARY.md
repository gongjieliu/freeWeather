# 天气 App 界面调整总结

## 调整日期
2026-03-24

---

## 1. 顶部导航栏调整

### 1.1 标题居中
- **文件**: `ui/navigation/WeatherNavigation.kt`
- **修改**: 将 "Weather Free" 标题居中显示
- **代码**: 使用 `Box` + `contentAlignment = Alignment.Center` 实现

### 1.2 移除底部导航栏
- **文件**: `ui/navigation/WeatherNavigation.kt`
- **修改**: 删除原来的底部导航栏 (`NavigationBar`)
- **功能**: 首页右上角添加三点菜单，可进入"管理城市"和"设置"

---

## 2. 首页界面调整

### 2.1 缩小顶部天气栏
- **文件**: `ui/home/HomeScreen.kt` - `CurrentLocationWeatherCard` 函数
- **修改内容**:
  - padding: 16dp → 12dp
  - 图标大小: 16dp → 14dp
  - 字体大小缩小
  - Spacer 高度减小

### 2.2 缩小城市间距
- **文件**: `ui/home/HomeScreen.kt` - LazyColumn
- **修改**:
  - contentPadding: 12dp → horizontal=12dp, vertical=8dp
  - verticalArrangement: 12dp → 8dp

### 2.3 城市框布局调整
- **文件**: `ui/home/HomeScreen.kt` - `CityWeatherCard` 函数
- **修改**:
  - 左侧：城市名称 (titleLarge, 粗体)
  - 右侧：当前温度 (titleLarge, 主色) + 最高/最低温度 + 天气状况（同一行）
  - 删除了城市名下的 Spacer

### 2.4 天气预报日期显示
- **文件**: `ui/home/HomeScreen.kt` - `ForecastItem` 函数
- **修改**: 根据日期显示标签
  - 今天 → "今天"
  - 明天 → "明天"
  - 后天 → "后天"
  - 其他 → 周几 (周一/周二...)
- **注意**: API 不支持昨天的天气，暂不显示

### 2.5 天气预报行缩小
- **文件**: `ui/home/HomeScreen.kt` - `ForecastItem` 函数
- **修改**:
  - RoundedCornerShape: 8dp → 6dp
  - padding: 8dp/6dp → 6dp/4dp
  - Spacer: 2dp

### 2.6 刷新按钮下移
- **文件**: `ui/home/HomeScreen.kt` - `Scaffold`
- **修改**: FAB 添加 `modifier = Modifier.padding(bottom = 16.dp)`

---

## 3. 管理城市界面调整

### 3.1 删除底部导航
- **文件**: `ui/city/CityScreen.kt`
- **修改**: 移除 TopAppBar，使用导航栏的统一返回箭头

### 3.2 简化箭头图标
- **文件**: `ui/city/CityScreen.kt`
- **修改**: 
  - 保留向上的箭头 `KeyboardArrowUp`（删除向下箭头）
  - 箭头大小: 32dp
  - 颜色: 主色 (primary)

### 3.3 提示文字更新
- **文件**: `ui/city/CityScreen.kt`
- **修改**: "←左滑删除 | ↑点上移"

### 3.4 移除长按拖动功能
- **文件**: `ui/city/CityScreen.kt`, `CityViewModel.kt`
- **修改**: 删除 `combinedClickable` 和相关拖动代码

---

## 4. 设置界面调整

### 4.1 删除顶部导航栏
- **文件**: `ui/settings/SettingsScreen.kt`
- **修改**: 移除 TopAppBar，使用导航栏的统一返回箭头

---

## 5. API 相关调整

### 5.1 天气预报 API 改为 7 天
- **文件**: `data/remote/WeatherApiService.kt`
- **修改**: `v7/weather/3d` → `v7/weather/7d`

---

## 关键代码文件清单

| 文件路径 | 功能 |
|---------|------|
| `ui/navigation/WeatherNavigation.kt` | 导航栏、标题、菜单 |
| `ui/home/HomeScreen.kt` | 首页布局、天气卡片、预报 |
| `ui/city/CityScreen.kt` | 城市管理界面 |
| `ui/settings/SettingsScreen.kt` | 设置界面 |
| `data/remote/WeatherApiService.kt` | API 端点定义 |

---

## 待优化项

1. **昨天的天气** - QWeather API 不支持，需换用付费 API 或其他服务商
2. **拖动排序** - 长按拖动排序功能未实现
