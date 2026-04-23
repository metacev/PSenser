package com.tracemaster.util.security

import com.tracemaster.domain.model.AntiCheatResult
import kotlin.math.sqrt

/**
 * 反作弊检测引擎 (PRD 8.2)
 * 
 * 功能：
 * - Mock Location 检测
 * - 速度合理性检测
 * - GPS 漂移检测
 * - 刷量行为检测
 */
class AntiCheatEngine {

    // 速度阈值 (m/s)
    private companion object {
        const val RUNNING_MAX_SPEED = 6.0f      // 跑步最大速度 21.6 km/h
        const val CYCLING_MAX_SPEED = 20.0f     // 骑行最大速度 72 km/h
        const val GPS_DRIFT_SPEED = 50.0f       // GPS 漂移速度 180 km/h
        const val SPEED_DURATION_THRESHOLD = 30000L // 超速持续时间阈值 (ms)
        const val MAX_TRACKS_PER_DAY = 20       // 每日最大轨迹数
        const val MIN_TRACK_DISTANCE = 100.0    // 最小有效轨迹距离 (米)
    }

    /**
     * 检测轨迹数据是否可疑
     */
    fun checkTrackData(
        points: List<Pair<Double, Double>>, // lat, lng
        timestamps: List<Long>,
        speeds: List<Float>?,
        isFromMockProvider: Boolean = false
    ): AntiCheatResult {
        val violations = mutableListOf<AntiCheatResult.ViolationType>()
        val evidence = mutableListOf<String>()
        var riskScore = 0.0f

        // 1. Mock Location 检测
        if (isFromMockProvider) {
            violations.add(AntiCheatResult.ViolationType.MOCK_LOCATION)
            evidence.add("检测到模拟器定位 (isFromMockProvider=true)")
            riskScore += 0.5f
        }

        // 2. 速度合理性检测
        if (!speeds.isNullOrEmpty()) {
            val speedViolations = checkSpeedViolations(speeds, timestamps)
            if (speedViolations.isNotEmpty()) {
                violations.addAll(speedViolations)
                evidence.add("检测到${speedViolations.size}次速度异常")
                riskScore += speedViolations.size * 0.1f
            }
        }

        // 3. GPS 漂移检测 (通过坐标计算速度)
        if (points.size >= 2 && timestamps.size >= 2) {
            val driftViolations = checkGpsDrift(points, timestamps)
            if (driftViolations > 0) {
                violations.add(AntiCheatResult.ViolationType.GPS_DRIFT)
                evidence.add("检测到$driftViolations 次 GPS 漂移 (直线速度>${GPS_DRIFT_SPEED}m/s)")
                riskScore += driftViolations * 0.15f
            }
        }

        // 计算最终风险评分 (0.0-1.0)
        riskScore = minOf(riskScore, 1.0f)

        // 确定建议操作
        val suggestedAction = when {
            riskScore >= 0.8f -> AntiCheatResult.SuggestedAction.BAN_USER
            riskScore >= 0.5f -> AntiCheatResult.SuggestedAction.AUTO_REJECT
            riskScore >= 0.3f -> AntiCheatResult.SuggestedAction.FLAG_FOR_REVIEW
            else -> AntiCheatResult.SuggestedAction.ALLOW
        }

        return AntiCheatResult(
            isSuspicious = riskScore >= 0.3f,
            riskScore = riskScore,
            violationTypes = violations.distinct(),
            evidence = evidence,
            suggestedAction = suggestedAction
        )
    }

    /**
     * 检测刷量行为
     */
    fun checkBrushingBehavior(
        tracksToday: Int,
        shortTracksCount: Int, // <100 米的轨迹数
        totalTimeMillis: Long
    ): AntiCheatResult {
        val violations = mutableListOf<AntiCheatResult.ViolationType>()
        val evidence = mutableListOf<String>()
        var riskScore = 0.0f

        // 检测每日创建数量
        if (tracksToday > MAX_TRACKS_PER_DAY) {
            violations.add(AntiCheatResult.ViolationType.BRUSHING_QUANTITY)
            evidence.add("24 小时内创建$tracksToday 条轨迹 (阈值:$MAX_TRACKS_PER_DAY)")
            riskScore += 0.4f
        }

        // 检测短轨迹频繁保存
        if (shortTracksCount > 5) {
            violations.add(AntiCheatResult.ViolationType.BRUSHING_QUANTITY)
            evidence.add("发现$shortTracksCount 条短轨迹 (<100 米)")
            riskScore += 0.3f
        }

        riskScore = minOf(riskScore, 1.0f)

        val suggestedAction = when {
            riskScore >= 0.7f -> AntiCheatResult.SuggestedAction.BAN_USER
            riskScore >= 0.4f -> AntiCheatResult.SuggestedAction.FLAG_FOR_REVIEW
            else -> AntiCheatResult.SuggestedAction.ALLOW
        }

        return AntiCheatResult(
            isSuspicious = riskScore >= 0.4f,
            riskScore = riskScore,
            violationTypes = violations.distinct(),
            evidence = evidence,
            suggestedAction = suggestedAction
        )
    }

    private fun checkSpeedViolations(
        speeds: List<Float>,
        timestamps: List<Long>
    ): List<AntiCheatResult.ViolationType> {
        val violations = mutableListOf<AntiCheatResult.ViolationType>()
        var runningOverSpeedCount = 0
        var cyclingOverSpeedCount = 0
        var lastOverSpeedTime: Long? = null

        for ((index, speed) in speeds.withIndex()) {
            val currentTime = timestamps.getOrElse(index) { 0L }

            // 检查跑步超速
            if (speed > RUNNING_MAX_SPEED) {
                if (lastOverSpeedTime == null || currentTime - lastOverSpeedTime!! < SPEED_DURATION_THRESHOLD) {
                    runningOverSpeedCount++
                } else {
                    runningOverSpeedCount = 1
                }
                lastOverSpeedTime = currentTime

                if (runningOverSpeedCount * 1000 >= SPEED_DURATION_THRESHOLD) {
                    violations.add(AntiCheatResult.ViolationType.UNREALISTIC_SPEED)
                }
            }

            // 检查骑行超速
            if (speed > CYCLING_MAX_SPEED) {
                cyclingOverSpeedCount++
                if (cyclingOverSpeedCount >= 3) {
                    violations.add(AntiCheatResult.ViolationType.UNREALISTIC_SPEED)
                }
            }
        }

        return violations.distinct()
    }

    private fun checkGpsDrift(
        points: List<Pair<Double, Double>>,
        timestamps: List<Long>
    ): Int {
        var driftCount = 0

        for (i in 1 until points.size) {
            val (lat1, lng1) = points[i - 1]
            val (lat2, lng2) = points[i]
            val timeDiff = timestamps[i] - timestamps[i - 1]

            if (timeDiff <= 0) continue

            val distance = haversineDistance(lat1, lng1, lat2, lng2)
            val speed = (distance / (timeDiff / 1000.0)).toFloat()

            if (speed > GPS_DRIFT_SPEED) {
                driftCount++
            }
        }

        return driftCount
    }

    /**
     * Haversine 公式计算两点间距离 (米)
     */
    private fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371000.0 // 地球半径 (米)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sinSquared(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                sinSquared(dLng / 2)
        val c = 2 * kotlin.math.atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun sinSquared(x: Double): Double {
        val sin = kotlin.math.sin(x)
        return sin * sin
    }
}
