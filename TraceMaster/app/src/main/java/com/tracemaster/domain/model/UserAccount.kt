package com.tracemaster.domain.model

/**
 * 用户订阅类型 (PRD 5.1)
 */
enum class SubscriptionTier {
    FREE,       // 免费版：5 条轨迹/500MB，30 天保留，每月导出 3 次
    PRO,        // 专业版：无限存储，永久保留，高级分析
    TEAM,       // 团队版：500GB 起，企业级归档
    ENTERPRISE  // 企业版：私有化部署，API 实时拉取
}

/**
 * 用户账户信息
 */
data class UserAccount(
    val userId: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val subscriptionExpiryDate: Long? = null, // 订阅过期时间戳
    val isGuest: Boolean = false, // 游客模式
    val createdAt: Long = System.currentTimeMillis(),
    // 游客模式限制
    val guestDataExpiryDate: Long? = null, // 游客数据过期时间（7 天）
    val maxGuestTracks: Int = 3 // 游客最大轨迹数
)

/**
 * 订阅权益配置 (PRD 5.1.1)
 */
data class SubscriptionEntitlements(
    val maxCloudTracks: Int, // 云端最大轨迹数
    val cloudStorageLimitMB: Long, // 云端存储限制 (MB)
    val dataRetentionDays: Int, // 数据保留天数 (-1=永久)
    val maxExportPerMonth: Int, // 每月最大导出次数 (-1=无限)
    val allowedExportFormats: List<ExportFormat>, // 允许的导出格式
    val hasAiAnalysis: Boolean, // AI 体能评估
    val hasHardwareSupport: Boolean, // 硬件接入支持
    val hasTeamFeatures: Boolean, // 团队功能
    val supportLevel: SupportLevel // 客服支持级别
) {
    enum class ExportFormat { GPX, KML, CSV, GEOJSON, PROTOBUF, API }
    enum class SupportLevel { COMMUNITY, EMAIL_48H, PHONE_247 }

    companion object {
        fun getEntitlements(tier: SubscriptionTier): SubscriptionEntitlements {
            return when (tier) {
                SubscriptionTier.FREE -> SubscriptionEntitlements(
                    maxCloudTracks = 5,
                    cloudStorageLimitMB = 500,
                    dataRetentionDays = 30,
                    maxExportPerMonth = 3, // 锚定效应设计
                    allowedExportFormats = listOf(ExportFormat.GPX, ExportFormat.KML),
                    hasAiAnalysis = false,
                    hasHardwareSupport = false,
                    hasTeamFeatures = false,
                    supportLevel = SupportLevel.COMMUNITY
                )
                SubscriptionTier.PRO -> SubscriptionEntitlements(
                    maxCloudTracks = -1,
                    cloudStorageLimitMB = 50 * 1024, // 50GB
                    dataRetentionDays = -1,
                    maxExportPerMonth = -1,
                    allowedExportFormats = listOf(
                        ExportFormat.GPX, ExportFormat.KML,
                        ExportFormat.CSV, ExportFormat.GEOJSON
                    ),
                    hasAiAnalysis = true,
                    hasHardwareSupport = true,
                    hasTeamFeatures = false,
                    supportLevel = SupportLevel.EMAIL_48H
                )
                SubscriptionTier.TEAM -> SubscriptionEntitlements(
                    maxCloudTracks = -1,
                    cloudStorageLimitMB = 500 * 1024, // 500GB
                    dataRetentionDays = -1,
                    maxExportPerMonth = -1,
                    allowedExportFormats = listOf(
                        ExportFormat.GPX, ExportFormat.KML,
                        ExportFormat.CSV, ExportFormat.GEOJSON,
                        ExportFormat.PROTOBUF
                    ),
                    hasAiAnalysis = true,
                    hasHardwareSupport = true,
                    hasTeamFeatures = true,
                    supportLevel = SupportLevel.PHONE_247
                )
                SubscriptionTier.ENTERPRISE -> SubscriptionEntitlements(
                    maxCloudTracks = -1,
                    cloudStorageLimitMB = -1,
                    dataRetentionDays = -1,
                    maxExportPerMonth = -1,
                    allowedExportFormats = ExportFormat.values().toList(),
                    hasAiAnalysis = true,
                    hasHardwareSupport = true,
                    hasTeamFeatures = true,
                    supportLevel = SupportLevel.PHONE_247
                )
            }
        }
    }
}

/**
 * 反作弊检测结果 (PRD 8.2)
 */
data class AntiCheatResult(
    val isSuspicious: Boolean,
    val riskScore: Float, // 0.0-1.0，越高越可疑
    val violationTypes: List<ViolationType>,
    val evidence: List<String>, // 证据描述
    val suggestedAction: SuggestedAction
) {
    enum class ViolationType {
        MOCK_LOCATION,      // 模拟器定位
        UNREALISTIC_SPEED,  // 不合理速度
        GPS_DRIFT,          // GPS 漂移
        BRUSHING_QUANTITY,  // 刷量行为
        ABNORMAL_CLUSTER    // 异常聚集
    }

    enum class SuggestedAction {
        ALLOW,              // 允许
        FLAG_FOR_REVIEW,    // 标记待审核
        AUTO_REJECT,        // 自动拒绝
        BAN_USER            // 封禁用户
    }
}

/**
 * 敏感区域检测结果 (PRD 8.1)
 */
data class SensitiveAreaResult(
    val isInSensitiveArea: Boolean,
    val areaType: AreaType?,
    val areaName: String?,
    val distance: Double, // 距离敏感区边界的距离 (米)
    val suggestedAction: SensitiveAction
) {
    enum class AreaType {
        MILITARY_ZONE,      // 军事禁区
        AIRPORT,            // 机场
        PORT,               // 港口
        GOVERNMENT_BUILDING,// 政府大院
        BORDER_REGION       // 边境地区
    }

    enum class SensitiveAction {
        ALLOW_RECORDING,        // 允许录制
        INTERRUPT_RECORDING,    // 中断录制（个人用户）
        MARK_CLASSIFIED,        // 标记涉密（企业用户）
        FUZZY_COORDINATES       // 坐标模糊化
    }
}

/**
 * 隐私脱敏配置 (PRD 8.1.3)
 */
data class PrivacyDesensitizationConfig(
    val enableStartEndProtection: Boolean = true, // 起点/终点保护
    val protectionRadiusMeters: Double = 500.0, // 保护半径
    val epsilon: Double = 1.0, // 差分隐私参数ε
    val enableTimeFuzzing: Boolean = true, // 时间模糊
    val timePrecisionSeconds: Int = 3600, // 时间精度（秒），默认 1 小时
    val enablePoiRemoval: Boolean = true, // POI 剔除
    val poiRemovalDistanceMeters: Double = 50.0, // 敏感 POI 剔除距离
    val dailyPrivacyBudget: Double = 10.0, // 每日隐私预算ε
    val shareCost: Double = 2.0 // 每次分享消耗的隐私预算
)
