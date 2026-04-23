package com.tracemaster.util.security

import com.tracemaster.domain.model.PrivacyDesensitizationConfig
import com.tracemaster.domain.model.SensitiveAreaResult
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.abs

/**
 * 隐私脱敏引擎 (PRD 8.1.3)
 * 
 * 功能：
 * - 起点/终点拉普拉斯噪声保护
 * - 时间模糊化
 * - 敏感 POI 剔除
 * - 差分隐私预算管理
 */
class PrivacyDesensitizationEngine(
    private val config: PrivacyDesensitizationConfig = PrivacyDesensitizationConfig()
) {

    private var remainingPrivacyBudget = config.dailyPrivacyBudget

    /**
     * 对轨迹进行脱敏处理
     * 
     * @param points 轨迹点列表 (lat, lng, timestamp)
     * @param isStartEndProtected 是否保护起点终点
     * @return 脱敏后的轨迹点
     */
    fun desensitizeTrack(
        points: List<Triple<Double, Double, Long>>, // lat, lng, timestamp
        isStartEndProtected: Boolean = config.enableStartEndProtection
    ): List<Triple<Double, Double, Long>> {
        if (points.isEmpty()) return emptyList()

        val result = mutableListOf<Triple<Double, Double, Long>>()

        for ((index, point) in points.withIndex()) {
            val (lat, lng, timestamp) = point
            var newLat = lat
            var newLng = lng
            var newTimestamp = timestamp

            // 1. 起点/终点保护 (添加拉普拉斯噪声)
            if (isStartEndProtected) {
                val isStartOrEnd = index < 5 || index >= points.size - 5
                if (isStartOrEnd && canUsePrivacyBudget()) {
                    val noise = generateLaplaceNoise(config.epsilon)
                    newLat = lat + noise * 0.0045 // 约 500 米范围
                    newLng = lng + noise * 0.0045
                    consumePrivacyBudget(config.shareCost)
                }
            }

            // 2. 时间模糊化
            if (config.enableTimeFuzzing) {
                newTimestamp = fuzzTimestamp(timestamp)
            }

            result.add(Triple(newLat, newLng, newTimestamp))
        }

        return result
    }

    /**
     * 检测并移除敏感 POI 附近的点
     */
    fun removeSensitivePoiPoints(
        points: List<Triple<Double, Double, Long>>,
        sensitivePois: List<Pair<Double, Double>> // 敏感 POI 坐标列表
    ): List<Triple<Double, Double, Long>> {
        if (!config.enablePoiRemoval) return points

        return points.filter { point ->
            val (lat, lng, _) = point
            sensitivePois.all { poi ->
                val distance = haversineDistance(lat, lng, poi.first, poi.second)
                distance > config.poiRemovalDistanceMeters
            }
        }
    }

    /**
     * 检查是否在敏感区域 (简化版，实际应接入国家测绘局坐标库)
     */
    fun checkSensitiveArea(
        lat: Double,
        lng: Double,
        sensitiveAreas: List<SensitiveArea> = DEFAULT_SENSITIVE_AREAS
    ): SensitiveAreaResult {
        for (area in sensitiveAreas) {
            val distance = haversineDistance(lat, lng, area.centerLat, area.centerLng)
            if (distance <= area.radiusMeters) {
                return SensitiveAreaResult(
                    isInSensitiveArea = true,
                    areaType = area.type,
                    areaName = area.name,
                    distance = distance,
                    suggestedAction = SensitiveAreaResult.SensitiveAction.INTERRUPT_RECORDING
                )
            }
        }

        return SensitiveAreaResult(
            isInSensitiveArea = false,
            areaType = null,
            areaName = null,
            distance = Double.MAX_VALUE,
            suggestedAction = SensitiveAreaResult.SensitiveAction.ALLOW_RECORDING
        )
    }

    /**
     * 重置每日隐私预算
     */
    fun resetDailyBudget() {
        remainingPrivacyBudget = config.dailyPrivacyBudget
    }

    /**
     * 获取剩余隐私预算
     */
    fun getRemainingBudget(): Double = remainingPrivacyBudget

    // ==================== 私有方法 ====================

    /**
     * 生成拉普拉斯噪声
     * Laplace(0, b) where b = sensitivity / epsilon
     */
    private fun generateLaplaceNoise(epsilon: Double): Double {
        val u = kotlin.math.random() - 0.5 // [-0.5, 0.5]
        val b = 1.0 / epsilon // 敏感度设为 1
        return -b * ln(1 - 2 * abs(u)) * if (u < 0) -1 else 1
    }

    /**
     * 时间模糊化 (精确到小时)
     */
    private fun fuzzTimestamp(timestamp: Long): Long {
        val precisionMillis = config.timePrecisionSeconds * 1000L
        return (timestamp / precisionMillis) * precisionMillis
    }

    /**
     * 检查是否有足够的隐私预算
     */
    private fun canUsePrivacyBudget(): Boolean {
        return remainingPrivacyBudget >= config.shareCost
    }

    /**
     * 消耗隐私预算
     */
    private fun consumePrivacyBudget(amount: Double) {
        remainingPrivacyBudget = maxOf(0.0, remainingPrivacyBudget - amount)
    }

    /**
     * Haversine 公式计算距离 (米)
     */
    private fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sinSquared(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                sinSquared(dLng / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun sinSquared(x: Double): Double {
        val sin = kotlin.math.sin(x)
        return sin * sin
    }

    /**
     * 敏感区域定义 (示例数据，实际应接入国家测绘局坐标库)
     */
    data class SensitiveArea(
        val name: String,
        val centerLat: Double,
        val centerLng: Double,
        val radiusMeters: Double,
        val type: SensitiveAreaResult.AreaType
    )

    companion object {
        // 示例敏感区域 (实际应从服务器动态获取)
        val DEFAULT_SENSITIVE_AREAS = listOf(
            SensitiveArea(
                name = "某军事禁区",
                centerLat = 39.9042,
                centerLng = 116.4074,
                radiusMeters = 1000.0,
                type = SensitiveAreaResult.AreaType.MILITARY_ZONE
            ),
            SensitiveArea(
                name = "某机场",
                centerLat = 40.0799,
                centerLng = 116.6031,
                radiusMeters = 5000.0,
                type = SensitiveAreaResult.AreaType.AIRPORT
            )
        )
    }
}
