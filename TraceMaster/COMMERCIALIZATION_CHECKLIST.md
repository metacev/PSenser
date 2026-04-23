# TraceMaster 商业化发布检查清单

## ✅ 已完成的功能

### 1. 权限处理
- [x] PermissionManager 权限管理工具类
- [x] 运行时权限请求逻辑
- [x] 权限拒绝解释对话框
- [x] 跳转到系统设置页面
- [x] MainActivity 集成权限请求

### 2. SQLCipher 数据加密
- [x] 添加 SQLCipher 依赖
- [x] TrackDatabase 支持加密模式
- [x] Application 初始化 SQLCipher
- [x] Hilt 模块配置加密数据库

### 3. 崩溃统计
- [x] Timber 日志库集成
- [x] CrashReportingTree 日志树
- [x] CrashHandler 全局异常处理器
- [x] 崩溃日志文件保存
- [x] Application 注册崩溃处理器
- [x] TODO 标记 Firebase Crashlytics 集成点

### 4. 隐私政策
- [x] PRIVACY_POLICY.md 文档

## ⚠️ 需要配置的项（发布前）

### 1. Firebase 配置
```kotlin
// 在 build.gradle.kts (project) 中添加 Google Services 插件
classpath("com.google.gms:google-services:4.4.0")

// 在 build.gradle.kts (app) 中添加
id("com.google.gms.google-services")
implementation("com.google.firebase:firebase-crashlytics-ktx:18.6.0")
implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")

// 在 CrashReportingTree.kt 和 CrashHandler.kt 中取消注释 Firebase 相关代码
```

### 2. 地图 SDK
- [ ] 申请高德地图或 Google Maps API Key
- [ ] 在 local.properties 中添加 MAPS_API_KEY
- [ ] 完善 MapScreen 的地图集成功能

### 3. 应用签名
```bash
# 生成签名密钥
keytool -genkey -v -keystore tracemaster-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias tracemaster

# 在 gradle.properties 中配置
storeFile=../tracemaster-release-key.jks
storePassword=your_password
keyAlias=tracemaster
keyPassword=your_password
```

### 4. 地图审图号（中国大陆发布必需）
- [ ] 向自然资源部申请互联网地图服务审图号
- [ ] 在应用内展示审图号

### 5. 合规要求
- [ ] ICP 备案（如在中国大陆运营）
- [ ] 网络安全等级保护备案
- [ ] 用户协议（需补充）
- [ ] 第三方 SDK 列表公示

### 6. 应用商店素材
- [ ] 应用图标（多尺寸）
- [ ] 应用截图（至少 3 张）
- [ ] 应用描述文案
- [ ] 宣传视频（可选）

## 📋 测试清单

### 功能测试
- [ ] 轨迹录制（前台/后台）
- [ ] 轨迹查看和编辑
- [ ] 轨迹导入导出（GPX/KML/GeoJSON）
- [ ] 权限授予/拒绝场景
- [ ] 数据库加密验证
- [ ] 崩溃恢复机制

### 性能测试
- [ ] 长时间录制稳定性（>2 小时）
- [ ] 大量轨迹点加载性能
- [ ] 内存泄漏检测
- [ ] 电池消耗测试

### 兼容性测试
- [ ] Android 8.0 (API 26)
- [ ] Android 10 (API 29)
- [ ] Android 13 (API 33)
- [ ] Android 14 (API 34)
- [ ] 主流品牌手机（华为、小米、OPPO、vivo 等）

## 🔒 安全加固建议

1. **代码混淆**: 已在 release 构建中启用 ProGuard
2. **防反编译**: 考虑使用梆梆安全、腾讯乐固等加固服务
3. **API 安全**: 所有网络请求使用 HTTPS
4. **敏感信息**: 不要将 API Key 硬编码在代码中

## 📱 发布渠道

- 华为应用市场
- 小米应用商店
- OPPO 软件商店
- vivo 应用商店
- 腾讯应用宝
- 豌豆荚
- Google Play（海外）

---

**注意**: 本清单仅供参考，实际发布时请根据最新法规要求和平台政策进行调整。
