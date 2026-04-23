package com.tracemaster.util.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tracemaster.R
import com.tracemaster.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 轨迹录制前台服务 - 保证后台持续录制
 */
@AndroidEntryPoint
class RecordingService : Service() {

    companion object {
        const val CHANNEL_ID = "recording_service_channel"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_PAUSE = "com.tracemaster.action.PAUSE"
        const val ACTION_RESUME = "com.tracemaster.action.RESUME"
        const val ACTION_STOP = "com.tracemaster.action.STOP"
        
        const val EXTRA_TRACK_ID = "extra_track_id"
        const val EXTRA_DISTANCE = "extra_distance"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_IS_PAUSED = "extra_is_paused"
    }

    @Inject
    lateinit var locationManager: com.tracemaster.util.location.LocationManager

    private var trackId: Long = -1
    private var isPaused = false
    private var currentDistance = 0.0
    private var currentDuration = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
            ACTION_STOP -> stopRecording()
            else -> startRecording(intent)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "轨迹录制服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于后台持续录制轨迹"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startRecording(intent: Intent?) {
        trackId = intent?.getLongExtra(EXTRA_TRACK_ID, -1) ?: -1
        isPaused = false
        
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun pauseRecording() {
        isPaused = true
        updateNotification()
    }

    private fun resumeRecording() {
        isPaused = false
        updateNotification()
    }

    private fun stopRecording() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pausePendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, RecordingService::class.java).apply { action = ACTION_PAUSE },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val resumePendingIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, RecordingService::class.java).apply { action = ACTION_RESUME },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopPendingIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, RecordingService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("轨迹录制中")
            .setContentText(buildContentText())
            .setSmallIcon(R.drawable.ic_location_on)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                R.drawable.ic_pause,
                if (isPaused) "继续" else "暂停",
                if (isPaused) resumePendingIntent else pausePendingIntent
            )
            .addAction(R.drawable.ic_stop, "停止", stopPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }

        return builder.build()
    }

    private fun updateNotification() {
        val notification = buildNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildContentText(): String {
        val distanceStr = String.format("%.2f km", currentDistance / 1000)
        val durationStr = formatDuration(currentDuration)
        return if (isPaused) {
            "已暂停 • $distanceStr • $durationStr"
        } else {
            "录制中 • $distanceStr • $durationStr"
        }
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, secs)
            minutes > 0 -> String.format("%d:%02d", minutes, secs)
            else -> String.format("%d秒", secs)
        }
    }

    fun updateStats(distance: Double, duration: Long) {
        currentDistance = distance
        currentDuration = duration
        updateNotification()
    }
}
