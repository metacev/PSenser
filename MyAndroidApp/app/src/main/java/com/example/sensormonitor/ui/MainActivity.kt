package com.example.sensormonitor.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.sensormonitor.R
import com.example.sensormonitor.databinding.ActivityMainBinding
import com.example.sensormonitor.model.*
import com.example.sensormonitor.util.DeviceInfoManager
import com.example.sensormonitor.util.SensorManagerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManagerUtil
    private lateinit var deviceInfoManager: DeviceInfoManager
    
    private var updateJob: Job? = null

    // Permission request launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            startMonitoring()
        } else {
            // Some permissions denied, continue with limited functionality
            startMonitoring()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize managers
        sensorManager = SensorManagerUtil(this)
        deviceInfoManager = DeviceInfoManager(this)

        // Setup UI
        setupUI()

        // Request permissions and start monitoring
        requestPermissions()
    }

    private fun setupUI() {
        // Set toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Sensor Monitor"
        
        // Swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            refreshAllData()
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        // Android 10+ (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        // Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        // Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Camera and Audio permissions (optional for status check)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        } else {
            startMonitoring()
        }
    }

    private fun startMonitoring() {
        // Register all sensors
        sensorManager.registerAllSensors()

        // Observe sensor data
        lifecycleScope.launch {
            sensorManager.sensorDataFlow.collectLatest { sensorData ->
                updateSensorDisplay(sensorData)
            }
        }

        // Start periodic updates for device info
        startPeriodicUpdates()
        
        // Initial load
        refreshAllData()
    }

    private fun startPeriodicUpdates() {
        updateJob?.cancel()
        updateJob = lifecycleScope.launch {
            while (true) {
                withContext(Dispatchers.Default) {
                    updateDeviceInfo()
                }
                delay(2000) // Update every 2 seconds
            }
        }
    }

    private fun refreshAllData() {
        updateDeviceInfo()
        binding.swipeRefresh.isRefreshing = false
    }

    private fun updateDeviceInfo() {
        try {
            // Device Info
            val deviceInfo = deviceInfoManager.getDeviceInfo()
            runOnUiThread {
                binding.deviceInfoText.text = """
                    Device: ${deviceInfo.manufacturer} ${deviceInfo.model}
                    Android: ${deviceInfo.androidVersion} (API ${deviceInfo.sdkInt})
                    Build: ${deviceInfo.buildNumber}
                    Memory: ${formatBytes(deviceInfo.availableMemory)} / ${formatBytes(deviceInfo.totalMemory)}
                """.trimIndent()
            }

            // Battery Info
            val batteryInfo = deviceInfoManager.getBatteryInfo()
            batteryInfo?.let {
                runOnUiThread {
                    binding.batteryInfoText.text = """
                        Level: ${it.level}% ${if (it.isCharging) "⚡" else ""}
                        Status: ${it.status}
                        Health: ${it.health}
                        Voltage: ${it.voltage} mV
                        Temperature: ${it.temperature}°C
                        Charger: ${it.chargerType}
                    """.trimIndent()
                }
            }

            // CPU Info
            val cpuInfo = deviceInfoManager.getCpuInfo()
            runOnUiThread {
                binding.cpuInfoText.text = """
                    Cores: ${cpuInfo.coreCount}
                    Frequency: ${cpuInfo.currentFreq} (Max: ${cpuInfo.maxFreq})
                    Temperature: ${cpuInfo.temperature?.toString() ?: "N/A"}°C
                """.trimIndent()
            }

            // Camera Info
            val cameraInfo = deviceInfoManager.getCameraInfo()
            runOnUiThread {
                binding.cameraInfoText.text = """
                    Cameras: ${cameraInfo.cameraCount}
                    Front: ${if (cameraInfo.hasFrontCamera) "✓" else "✗"}
                    Back: ${if (cameraInfo.hasBackCamera) "✓" else "✗"}
                    IDs: ${cameraInfo.cameraIds.joinToString(", ")}
                """.trimIndent()
            }

            // Audio Info
            val audioInfo = deviceInfoManager.getAudioInfo()
            runOnUiThread {
                binding.audioInfoText.text = """
                    Microphone: ${if (audioInfo.hasMicrophone) "✓" else "✗"}
                    Speaker: ${if (audioInfo.hasSpeaker) "✓" else "✗"}
                    Volume: ${audioInfo.mediaVolume}/${audioInfo.maxVolume}
                """.trimIndent()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateSensorDisplay(sensorData: Map<String, SensorData>) {
        runOnUiThread {
            val sensorList = StringBuilder()
            
            if (sensorData.isEmpty()) {
                sensorList.append("No sensors detected or data available\n")
            } else {
                sensorData.values.forEach { data ->
                    sensorList.append("${data.name}: ${data.value} ${data.unit}\n")
                }
            }
            
            binding.sensorDataText.text = sensorList.toString()
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensors to save battery
        sensorManager.unregisterAllSensors()
        updateJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        // Re-register sensors when app comes back
        if (::sensorManager.isInitialized) {
            sensorManager.registerAllSensors()
            startPeriodicUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.release()
        updateJob?.cancel()
    }
}
