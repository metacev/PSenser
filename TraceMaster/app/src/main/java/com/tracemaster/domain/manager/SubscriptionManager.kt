package com.tracemaster.domain.manager

import com.tracemaster.domain.model.SubscriptionEntitlements
import com.tracemaster.domain.model.SubscriptionTier
import com.tracemaster.domain.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 订阅管理器 (PRD 5.1)
 * 
 * 功能：
 * - 管理用户订阅状态
 * - 检查功能权限
 * - 追踪导出次数限制
 * - 游客模式管理
 */
class SubscriptionManager {

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _exportCountThisMonth = MutableStateFlow(0)
    val exportCountThisMonth: StateFlow<Int> = _exportCountThisMonth.asStateFlow()

    private var currentMonth = 0

    /**
     * 设置当前用户
     */
    fun setUser(account: UserAccount) {
        _currentUser.value = account
        resetExportCounterIfNeeded()
    }

    /**
     * 清除用户 (登出)
     */
    fun clearUser() {
        _currentUser.value = null
    }

    /**
     * 获取当前订阅等级
     */
    fun getCurrentTier(): SubscriptionTier {
        return _currentUser.value?.subscriptionTier ?: SubscriptionTier.FREE
    }

    /**
     * 获取当前订阅权益
     */
    fun getCurrentEntitlements(): SubscriptionEntitlements {
        val tier = getCurrentTier()
        return SubscriptionEntitlements.getEntitlements(tier)
    }

    /**
     * 检查是否可以导出
     * 
     * @return Pair(canExport, reason)
     */
    fun canExport(): Pair<Boolean, String> {
        val user = _currentUser.value ?: return Pair(false, "请先登录")
        val entitlements = getCurrentEntitlements()

        // 检查导出格式是否允许
        // (调用方需传入格式，此处仅检查次数)

        // 检查每月导出次数限制
        if (entitlements.maxExportPerMonth != -1) {
            resetExportCounterIfNeeded()
            if (_exportCountThisMonth.value >= entitlements.maxExportPerMonth) {
                return Pair(
                    false,
                    "免费版每月仅限${entitlements.maxExportPerMonth}次导出，升级 Pro 解锁无限导出"
                )
            }
        }

        return Pair(true, "")
    }

    /**
     * 记录一次导出
     */
    fun recordExport() {
        resetExportCounterIfNeeded()
        _exportCountThisMonth.value++
    }

    /**
     * 检查是否有 AI 分析权限
     */
    fun hasAiAnalysis(): Boolean {
        return getCurrentEntitlements().hasAiAnalysis
    }

    /**
     * 检查是否有硬件接入权限
     */
    fun hasHardwareSupport(): Boolean {
        return getCurrentEntitlements().hasHardwareSupport
    }

    /**
     * 检查是否有团队功能权限
     */
    fun hasTeamFeatures(): Boolean {
        return getCurrentEntitlements().hasTeamFeatures
    }

    /**
     * 检查是否是游客模式
     */
    fun isGuest(): Boolean {
        return _currentUser.value?.isGuest ?: true
    }

    /**
     * 检查游客数据是否过期
     */
    fun isGuestDataExpired(): Boolean {
        val user = _currentUser.value ?: return true
        if (!user.isGuest) return false
        
        val expiryDate = user.guestDataExpiryDate ?: return false
        return System.currentTimeMillis() > expiryDate
    }

    /**
     * 检查是否可以创建新轨迹 (游客模式限制)
     */
    fun canCreateTrack(currentTrackCount: Int): Pair<Boolean, String> {
        val user = _currentUser.value
        
        if (user?.isGuest == true) {
            if (currentTrackCount >= user.maxGuestTracks) {
                return Pair(
                    false,
                    "游客模式最多存储${user.maxGuestTracks}条轨迹，注册账号永久保存"
                )
            }
            
            val expiryDate = user.guestDataExpiryDate
            if (expiryDate != null && System.currentTimeMillis() > expiryDate) {
                return Pair(
                    false,
                    "游客数据已过期（7 天），注册账号永久保存"
                )
            }
        }

        return Pair(true, "")
    }

    /**
     * 检查云端存储是否超限
     */
    fun isCloudStorageExceeded(currentStorageMB: Long): Boolean {
        val entitlements = getCurrentEntitlements()
        if (entitlements.cloudStorageLimitMB == -1L) return false
        return currentStorageMB >= entitlements.cloudStorageLimitMB
    }

    /**
     * 获取付费墙提示信息
     */
    fun getPaywallMessage(feature: String): String {
        return when (feature) {
            "ai_analysis" -> "AI 体能评估和训练建议是 Pro 会员专属功能"
            "export_advanced" -> "CSV/GeoJSON 导出是 Pro 会员专属功能"
            "hardware" -> "心率带/功率计接入是 Pro 会员专属功能"
            "team" -> "团队管理是企业版专属功能"
            else -> "该功能是 Pro 会员专属功能"
        }
    }

    /**
     * 重置导出计数器 (每月重置)
     */
    private fun resetExportCounterIfNeeded() {
        val currentCalendarMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        if (currentCalendarMonth != currentMonth) {
            currentMonth = currentCalendarMonth
            _exportCountThisMonth.value = 0
        }
    }

    /**
     * 模拟升级订阅 (实际应通过支付回调)
     */
    fun upgradeSubscription(tier: SubscriptionTier, expiryDate: Long) {
        val currentUser = _currentUser.value ?: return
        val updatedUser = currentUser.copy(
            subscriptionTier = tier,
            subscriptionExpiryDate = expiryDate,
            isGuest = false
        )
        _currentUser.value = updatedUser
    }

    /**
     * 检查订阅是否过期
     */
    fun isSubscriptionExpired(): Boolean {
        val user = _currentUser.value ?: return true
        if (user.subscriptionTier == SubscriptionTier.FREE) return false
        
        val expiryDate = user.subscriptionExpiryDate ?: return false
        return System.currentTimeMillis() > expiryDate
    }
}
