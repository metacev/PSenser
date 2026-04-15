# Sensor Monitor - Android 传感器监控应用

## 📱 项目简介

一款适用于 **Android 10 (API 29)** 到 **Android 16 (API 36)** 的传感器实时监控应用，提供科技感十足的深色主题界面。

## ✨ 核心功能

### 1. 传感器实时监控
- **加速度计** - 三轴加速度数据 (m/s²)
- **陀螺仪** - 角速度监测 (rad/s)
- **磁力计** - 磁场强度 (μT)
- **气压计** - 大气压力 (hPa)
- **光线传感器** - 环境光强度 (lux)
- **距离传感器** - 接近检测 (cm)
- **温度/湿度传感器** - 环境监测
- **心率传感器** - 健康监测 (bpm)
- **步数计数器** - 运动追踪
- **重力传感器** - 重力方向
- **线性加速度** - 去除重力影响的加速度
- **旋转矢量** - 设备方向
- **显著运动检测** - 低功耗运动感知
- **静态/动态检测** - 设备状态识别
- **心跳检测** - 心跳事件
- **头部追踪** - 6DoF 姿态追踪

### 2. 设备信息展示
- 设备型号、品牌、制造商
- Android 版本及代号 (支持 Android 10-16)
- SDK 版本号
- 屏幕分辨率和密度
- RAM 总量和可用量
- 存储空间总量和可用量
- CPU 核心数和架构
- 构建号和安全补丁

### 3. 硬件状态监控
- **电池**: 电量百分比、充电状态、健康度、温度、电压
- **CPU**: 活跃核心数、频率、温度 (需 root)
- **相机**: 前后摄像头状态、摄像头总数
- **音频**: 麦克风/扬声器可用性、音量级别
- **网络**: Wi-Fi 状态、移动数据状态、IP 地址

## 🔧 技术特性

### 统一版本管理
所有版本号集中在 `gradle.properties` 中管理，防止版本冲突：
```properties
compileSdkVersion=36
minSdkVersion=29
targetSdkVersion=36
agpVersion=8.7.0
javaVersion=VERSION_17
```

### 兼容性处理
- ✅ Android 10-16 全版本兼容
- ✅ 权限分级请求 (Android 13+ 通知权限)
- ✅ 硬件特性可选声明 (确保无特定传感器设备可运行)
- ✅ 新版本名称显示 (Android 10-16 完整代号)

### 架构设计
- **MVVM 模式** - 清晰的职责分离
- **ViewBinding** - 类型安全的视图绑定
- **Kotlin Coroutines** - 异步数据处理
- **LiveData/StateFlow** - 响应式 UI 更新
- **RecyclerView + DiffUtil** - 高效列表更新

## 🎨 UI 设计

### 科技感深色主题
- **主背景**: `#0D1117` - 深空黑
- **卡片背景**: `#161B22` - 碳素灰
- **强调色**: 
  - 青色 `#00E5FF` (主色调)
  - 绿色 `#00FF88` (CPU)
  - 紫色 `#BB66FF` (相机)
  - 橙色 `#FF9F43` (音频)
  - 蓝色 `#5C7CFA` (网络)

### 界面布局
- **Tab 导航**: 传感器 / 设备信息 / 硬件状态
- **卡片式设计**: Material Design 3 风格
- **实时数据**: 等宽字体显示数值
- **进度条**: 渐变色电量指示

## 📁 项目结构

```
SensorMonitor/
├── gradle.properties              # 统一版本配置
├── build.gradle.kts               # 项目级构建配置
├── settings.gradle.kts            # 项目设置
├── gradle/wrapper/
│   └── gradle-wrapper.properties  # Gradle 包装器配置
├── app/
│   ├── build.gradle.kts           # 应用级构建配置
│   ├── proguard-rules.pro         # ProGuard 规则
│   └── src/main/
│       ├── AndroidManifest.xml    # 应用清单
│       ├── java/com/example/sensormonitor/
│       │   ├── ui/                # UI 组件
│       │   │   ├── MainActivity.kt
│       │   │   ├── SensorListFragment.kt
│       │   │   ├── DeviceInfoFragment.kt
│       │   │   ├── HardwareStatusFragment.kt
│       │   │   └── SensorDataAdapter.kt
│       │   ├── model/             # 数据模型
│       │   │   └── SensorData.kt
│       │   └── util/              # 工具类
│       │       ├── SensorDataManager.kt
│       │       ├── DeviceInfoManager.kt
│       │       └── HardwareStatusManager.kt
│       └── res/
│           ├── layout/            # 布局文件
│           ├── values/            # 资源值
│           ├── drawable/          # 图形资源
│           └── mipmap-*/          # 应用图标
└── README.md                      # 项目文档
```

## 🚀 使用方法

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK API 36

### 构建步骤

1. **打开项目**
   ```
   File > Open > 选择 SensorMonitor 目录
   ```

2. **同步 Gradle**
   ```
   File > Sync Project with Gradle Files
   ```

3. **构建 APK**
   ```bash
   ./gradlew assembleDebug
   ```
   或使用菜单: `Build > Build APK(s)`

4. **运行应用**
   - 连接 Android 10+ 设备
   - 启用 USB 调试
   - 点击 Run 按钮

### 本地属性配置 (如需要)
在 `local.properties` 中添加:
```properties
sdk.dir=/path/to/your/Android/sdk
```

## ⚠️ 注意事项

### 权限说明
- `BODY_SENSORS`: 读取传感器数据 (Android 10+)
- `POST_NOTIFICATIONS`: 发送通知 (Android 13+)
- `CAMERA`: 检测相机状态 (不使用相机)
- `RECORD_AUDIO`: 检测麦克风状态 (不录音)

### 功能限制
- **CPU 温度**: 部分设备需要 root 权限才能读取
- **CPU 频率**: 某些设备可能无法访问
- **传感器可用性**: 取决于设备硬件配置
- **电池温度**: 精度因设备而异

### 兼容性
- 所有硬件特性声明为 `optional`，确保在无特定硬件的设备上也能运行
- 缺失的传感器会显示"无数据"
- 应用会自动适配不同屏幕尺寸

## 📊 版本历史

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| 1.0 | 2024 | 初始版本，支持 Android 10-16 |

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

**开发团队**: Sensor Monitor Team  
**最后更新**: 2024
