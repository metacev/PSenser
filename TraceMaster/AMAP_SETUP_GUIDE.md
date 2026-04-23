# 高德地图 SDK 集成指南

## 1. 申请 API Key

### 步骤：
1. 访问 [高德开放平台](https://console.amap.com/)
2. 注册/登录账号
3. 进入"应用管理" → "我的应用"
4. 点击"创建新应用"
5. 填写应用信息（应用名称、类型等）
6. 添加 Key：
   - **Key 名称**: TraceMaster
   - **服务平台**: Android SDK
   - **SHA1**: 需要获取开发版和发布版的 SHA1
   - **包名**: com.tracemaster

### 获取 SHA1：
```bash
# 调试版本
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# 发布版本（替换为您的 keystore 路径）
keytool -list -v -keystore /path/to/your/release.keystore -alias your_alias -storepass your_password
```

## 2. 配置 API Key

### 方法一：在 local.properties 中配置（推荐）
在项目根目录的 `local.properties` 文件中添加：
```properties
AMAP_API_KEY=您的高德地图API密钥
```

### 方法二：在 gradle.properties 中配置
在 `gradle.properties` 文件中添加：
```properties
AMAP_API_KEY=您的高德地图API密钥
```

### 方法三：直接在 build.gradle.kts 中配置（不推荐用于生产环境）
修改 `app/build.gradle.kts`：
```kotlin
android {
    defaultConfig {
        manifestPlaceholders["AMAP_API_KEY"] = "您的高德地图API密钥"
    }
}
```

## 3. 编译运行

完成上述配置后，即可编译运行项目。

## 4. 主要功能

### AMapManager 提供的功能：
- ✅ 地图初始化与配置
- ✅ 相机移动（到指定位置/边界）
- ✅ 轨迹绘制（Polyline）
- ✅ 实时位置标记更新
- ✅ 添加/清除标记
- ✅ 地图截图
- ✅ 3D 建筑、室内地图支持
- ✅ 手势控制（缩放、旋转、倾斜）

### 已集成的 UI 组件：
- ✅ AMapScreen - 主地图界面
- ✅ AndroidMapView - Compose 中的 MapView 包装器

## 5. 注意事项

1. **权限要求**：确保在 AndroidManifest.xml 中已声明位置权限
2. **网络要求**：地图加载需要网络连接
3. **ProGuard 配置**：发布版本需配置混淆规则（已在 proguard-rules.pro 中配置）
4. **SDK 版本**：当前使用 latest.integration，建议固定版本号以确保稳定性

## 6. 高级用法

### 自定义轨迹样式
```kotlin
aMapManager.drawTrack(amap, trackPoints) // 默认粉色轨迹
// 可修改 AMapManager.kt 中的颜色参数
```

### 轨迹回放
```kotlin
// 使用 updateLocationMarker 方法逐点更新位置
// 配合协程延迟实现动画效果
```

### 离线地图
高德地图支持离线地图下载，可在设置中添加离线地图管理功能。

## 7. 相关文档

- [高德地图 Android SDK 文档](https://lbs.amap.com/api/android-sdk/guide)
- [高德地图常见问题](https://lbs.amap.com/faq/android-sdk/183)
- [Android Manifest 配置说明](https://lbs.amap.com/api/android-sdk/guide/create-project/android-studio-create-project)

## 8. 商业化注意事项

1. **审图号**：上线前需在应用内展示高德地图审图号
2. **版权标识**：保留高德地图版权信息
3. **配额限制**：注意 API 调用配额，商业应用可能需要购买服务
4. **隐私政策**：在隐私政策中说明使用高德地图 SDK
