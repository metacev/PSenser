package com.tracemaster

import android.app.Application
import com.tracemaster.crash.CrashHandler
import com.tracemaster.crash.CrashReportingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * TraceMaster 应用入口类
 * 
 * 初始化内容：
 * 1. Hilt 依赖注入
 * 2. Timber 日志系统（集成崩溃统计）
 * 3. SQLCipher 数据库加密库
 * 4. 全局崩溃处理器
 */
@HiltAndroidApp
class TraceMasterApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化全局崩溃处理器
        initCrashHandler()
        
        // 初始化 Timber 日志系统
        initTimber()
        
        // 初始化 SQLCipher（确保在访问数据库前调用）
        initSQLCipher()
    }
    
    /**
     * 初始化全局崩溃处理器
     */
    private fun initCrashHandler() {
        val crashHandler = CrashHandler.init(this)
        crashHandler.register()
        Timber.d("CrashHandler initialized")
    }
    
    /**
     * 初始化 Timber 日志系统
     * - Debug 构建：输出完整日志到 Logcat
     * - Release 构建：仅记录错误和崩溃到 Crashlytics
     */
    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            // Debug 模式：使用调试树，输出所有日志
            Timber.plant(Timber.DebugTree())
        } else {
            // Release 模式：使用崩溃报告树，仅记录错误和异常
            Timber.plant(CrashReportingTree())
        }
        
        Timber.d("TraceMaster Application started")
    }
    
    /**
     * 初始化 SQLCipher 加密库
     * 加载 native 库以支持数据库加密
     */
    private fun initSQLCipher() {
        try {
            // 加载 SQLCipher native 库
            net.sqlcipher.database.SQLiteDatabase.loadLibs(this)
            Timber.d("SQLCipher initialized successfully")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize SQLCipher")
            // SQLCipher 初始化失败时，应用仍可运行但数据库不加密
            // 在生产环境中应该考虑是否允许这种情况
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // 清理资源
        com.tracemaster.data.local.database.TrackDatabase.closeDatabase()
        Timber.d("TraceMaster Application terminated")
    }
}
