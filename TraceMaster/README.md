
## 高德地图集成

本项目已集成高德地图 SDK，替代了原有的 Google Maps。

### 快速开始

1. **获取 API Key**：访问 [高德开放平台](https://console.amap.com/) 创建应用并获取 Android SDK 的 API Key

2. **配置 Key**：在 `local.properties` 文件中添加：
   ```properties
   AMAP_API_KEY=您的 API 密钥
   ```

3. **编译运行**：正常编译项目即可

详细配置说明请参考 [AMAP_SETUP_GUIDE.md](AMAP_SETUP_GUIDE.md)

### 主要功能

- 🗺️ 实时地图显示与交互
- 📍 轨迹绘制与回放
- 🎯 实时位置定位
- 🏢 3D 建筑与室内地图
- 📷 地图截图

