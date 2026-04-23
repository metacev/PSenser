package com.tracemaster.crash

import android.util.Log
import timber.log.Timber

/**
 * 崩溃报告日志树
 * 将 Timber 日志集成到 Firebase Crashlytics 或其他崩溃统计服务
 */
class CrashReportingTree : Timber.Tree() {
    
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // 跳过 Verbose 和 Debug 级别，避免过多日志
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        // 记录异常到崩溃统计服务
        t?.let { throwable ->
            when (priority) {
                Log.ERROR -> recordException(throwable, "ERROR: $message")
                Log.WARN -> recordException(throwable, "WARN: $message")
                Log.ASSERT -> recordException(throwable, "ASSERT: $message")
            }
        }
        
        // 记录非异常的错误日志
        if (t == null && (priority == Log.ERROR || priority == Log.ASSERT)) {
            recordLog(priority, tag, message)
        }
    }
    
    /**
     * 记录异常到崩溃统计服务
     * 实际项目中应替换为 Firebase Crashlytics.recordException()
     */
    private fun recordException(throwable: Throwable, message: String) {
        // TODO: 集成 Firebase Crashlytics
        // FirebaseCrashlytics.getInstance().recordException(throwable)
        // FirebaseCrashlytics.getInstance().log(message)
        
        // 临时实现：打印到控制台
        Log.e("CrashReporting", "Recorded exception: $message", throwable)
    }
    
    /**
     * 记录日志到崩溃统计服务
     * 实际项目中应替换为 Firebase Crashlytics.log()
     */
    private fun recordLog(priority: Int, tag: String?, message: String) {
        // TODO: 集成 Firebase Crashlytics
        // FirebaseCrashlytics.getInstance().log("[$tag] $message")
        
        // 临时实现：打印到控制台
        Log.println(priority, tag ?: "CrashReporting", message)
    }
}
