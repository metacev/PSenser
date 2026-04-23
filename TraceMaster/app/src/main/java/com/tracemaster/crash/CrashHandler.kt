package com.tracemaster.crash

import android.content.Context
import android.os.Build
import com.tracemaster.data.local.database.TrackDatabase
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 全局崩溃处理器
 * 
 * 功能：
 * 1. 捕获未处理的异常
 * 2. 保存崩溃日志到文件
 * 3. 记录到崩溃统计服务（Firebase Crashlytics）
 * 4. 优雅地关闭应用
 */
class CrashHandler private constructor(
    private val context: Context
) : Thread.UncaughtExceptionHandler {

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var isHandlingCrash = false
    
    companion object {
        @Volatile
        private var instance: CrashHandler? = null
        
        fun init(context: Context): CrashHandler {
            return instance ?: synchronized(this) {
                val newInstance = CrashHandler(context.applicationContext)
                instance = newInstance
                newInstance
            }
        }
    }
    
    /**
     * 注册为全局异常处理器
     * 应在 Application.onCreate() 中调用
     */
    fun register() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        Timber.d("CrashHandler registered as global exception handler")
    }
    
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        if (isHandlingCrash) {
            // 防止重复处理
            defaultHandler?.uncaughtException(thread, throwable)
            return
        }
        
        isHandlingCrash = true
        
        try {
            // 1. 记录崩溃信息到 Timber（会转发到 Crashlytics）
            val crashInfo = buildCrashInfo(thread, throwable)
            Timber.e(throwable, crashInfo)
            
            // 2. 保存崩溃日志到文件
            saveCrashLogToFile(crashInfo, throwable)
            
            // 3. 清理资源
            cleanup()
            
            // 4. 延迟退出，确保日志已保存
            android.os.Process.killProcess(android.os.Process.myPid())
        } catch (e: Exception) {
            Timber.e(e, "Error while handling crash")
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    /**
     * 构建崩溃信息字符串
     */
    private fun buildCrashInfo(thread: Thread, throwable: Throwable): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        throwable.printStackTrace(printWriter)
        printWriter.close()
        
        return buildString {
            appendLine("========== CRASH REPORT ==========")
            appendLine("Time: $timestamp")
            appendLine("App Version: ${getAppVersion()}")
            appendLine("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Thread: ${thread.name} (${thread.id})")
            appendLine("----------------------------------")
            appendLine(writer.toString())
            appendLine("==================================")
        }
    }
    
    /**
     * 保存崩溃日志到文件
     */
    private fun saveCrashLogToFile(crashInfo: String, throwable: Throwable) {
        try {
            val timestamp = System.currentTimeMillis()
            val fileName = "crash_${timestamp}.log"
            
            // 保存到应用缓存目录
            val logFile = java.io.File(context.cacheDir, "crash_logs").apply {
                mkdirs()
            }
            
            val file = java.io.File(logFile, fileName)
            file.writeText(crashInfo)
            
            Timber.d("Crash log saved to: ${file.absolutePath}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save crash log to file")
        }
    }
    
    /**
     * 清理资源
     */
    private fun cleanup() {
        try {
            // 关闭数据库连接
            TrackDatabase.closeDatabase()
            Timber.d("Database connections closed")
        } catch (e: Exception) {
            Timber.e(e, "Error closing database during crash")
        }
    }
    
    /**
     * 获取应用版本号
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
            "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * 手动记录非致命异常
     * 用于捕获不会导致应用崩溃的异常
     */
    fun logNonFatalException(throwable: Throwable, message: String? = null) {
        Timber.e(throwable, message ?: "Non-fatal exception")
        // TODO: 集成 Firebase Crashlytics.recordException(throwable)
    }
    
    /**
     * 记录自定义日志到崩溃统计服务
     */
    fun log(message: String) {
        Timber.d(message)
        // TODO: 集成 Firebase Crashlytics.log(message)
    }
    
    /**
     * 设置用户标识符（用于崩溃统计）
     */
    fun setUserId(userId: String) {
        // TODO: 集成 Firebase Crashlytics.setUserId(userId)
        Timber.d("User ID set: $userId")
    }
    
    /**
     * 设置自定义键值对（用于崩溃统计）
     */
    fun setCustomKey(key: String, value: String) {
        // TODO: 集成 Firebase Crashlytics.setCustomKey(key, value)
        Timber.d("Custom key set: $key = $value")
    }
}
