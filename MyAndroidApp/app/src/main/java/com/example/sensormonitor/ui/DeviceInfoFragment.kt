package com.example.sensormonitor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sensormonitor.R
import com.example.sensormonitor.util.DeviceInfoManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Fragment to display device hardware information (CPU, Battery, Camera, Audio)
 */
class DeviceInfoFragment : Fragment() {
    
    private var deviceInfoManager: DeviceInfoManager? = null
    
    // CPU Views
    private var textViewCpuCores: TextView? = null
    private var textViewCpuFreq: TextView? = null
    private var textViewCpuTemp: TextView? = null
    
    // Battery Views
    private var textViewBatteryLevel: TextView? = null
    private var textViewBatteryStatus: TextView? = null
    private var textViewBatteryTemp: TextView? = null
    private var textViewBatteryVoltage: TextView? = null
    private var textViewBatteryHealth: TextView? = null
    
    // Camera Views
    private var textViewCameraInfo: TextView? = null
    
    // Audio Views
    private var textViewAudioInfo: TextView? = null
    
    // Device Info Views
    private var textViewDeviceName: TextView? = null
    private var textViewDeviceModel: TextView? = null
    private var textViewAndroidVersion: TextView? = null
    private var textViewMemoryInfo: TextView? = null
    
    private var updateJob: Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceInfoManager = DeviceInfoManager(requireContext())
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_device_info, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize CPU views
        textViewCpuCores = view.findViewById(R.id.textViewCpuCores)
        textViewCpuFreq = view.findViewById(R.id.textViewCpuFreq)
        textViewCpuTemp = view.findViewById(R.id.textViewCpuTemp)
        
        // Initialize Battery views
        textViewBatteryLevel = view.findViewById(R.id.textViewBatteryLevel)
        textViewBatteryStatus = view.findViewById(R.id.textViewBatteryStatus)
        textViewBatteryTemp = view.findViewById(R.id.textViewBatteryTemp)
        textViewBatteryVoltage = view.findViewById(R.id.textViewBatteryVoltage)
        textViewBatteryHealth = view.findViewById(R.id.textViewBatteryHealth)
        
        // Initialize Camera views
        textViewCameraInfo = view.findViewById(R.id.textViewCameraInfo)
        
        // Initialize Audio views
        textViewAudioInfo = view.findViewById(R.id.textViewAudioInfo)
        
        // Initialize Device Info views
        textViewDeviceName = view.findViewById(R.id.textViewDeviceName)
        textViewDeviceModel = view.findViewById(R.id.textViewDeviceModel)
        textViewAndroidVersion = view.findViewById(R.id.textViewAndroidVersion)
        textViewMemoryInfo = view.findViewById(R.id.textViewMemoryInfo)
        
        startUpdating()
    }
    
    private fun startUpdating() {
        updateJob = lifecycleScope.launch {
            while (isActive) {
                updateAllInfo()
                delay(1000) // Update every second
            }
        }
    }
    
    private fun updateAllInfo() {
        activity?.runOnUiThread {
            updateCpuInfo()
            updateBatteryInfo()
            updateCameraInfo()
            updateAudioInfo()
            updateDeviceInfo()
        }
    }
    
    private fun updateCpuInfo() {
        val cpuInfo = deviceInfoManager?.getCpuInfo() ?: return
        textViewCpuCores?.text = "Cores: ${cpuInfo.coreCount}"
        textViewCpuFreq?.text = "Frequencies: ${cpuInfo.frequencies.take(4).joinToString(", ")}${if (cpuInfo.frequencies.size > 4) "..." else ""} MHz"
        textViewCpuTemp?.text = "Temperature: ${String.format("%.1f", cpuInfo.temperature)}°C"
    }
    
    private fun updateBatteryInfo() {
        val batteryInfo = deviceInfoManager?.getBatteryInfo() ?: return
        textViewBatteryLevel?.text = "Level: ${batteryInfo.level}%"
        textViewBatteryStatus?.text = "Status: ${batteryInfo.chargeType}"
        textViewBatteryTemp?.text = "Temperature: ${String.format("%.1f", batteryInfo.temperature)}°C"
        textViewBatteryVoltage?.text = "Voltage: ${batteryInfo.voltage} mV"
        
        val healthText = when (batteryInfo.health) {
            1 -> "Unknown"
            2 -> "Good"
            3 -> "Overheat"
            4 -> "Dead"
            5 -> "Over Voltage"
            6 -> "Unspecified Failure"
            7 -> "Cold"
            else -> "Unknown"
        }
        textViewBatteryHealth?.text = "Health: $healthText"
    }
    
    private fun updateCameraInfo() {
        val cameras = deviceInfoManager?.getCameraStatusList() ?: return
        val cameraText = cameras.mapIndexed { index, camera ->
            val facingText = when (camera.facing) {
                0 -> "Back"
                1 -> "Front"
                2 -> "External"
                else -> "Unknown"
            }
            "Camera $index: $facingText (${if (camera.isAvailable) "Available" else "Unavailable"})"
        }.joinToString("\n")
        
        textViewCameraInfo?.text = if (cameraText.isEmpty()) "No cameras found" else cameraText
    }
    
    private fun updateAudioInfo() {
        val audioInfo = deviceInfoManager?.getAudioDeviceStatus() ?: return
        val micStatus = if (audioInfo.isMicrophoneAvailable) "✓ Available" else "✗ Not Available"
        val speakerStatus = if (audioInfo.isSpeakerAvailable) "✓ Available" else "✗ Not Available"
        val volumeText = "Volume: ${audioInfo.currentVolume}/${audioInfo.maxVolume}${if (audioInfo.isMuted) " (Muted)" else ""}"
        
        textViewAudioInfo?.text = "$micStatus\n$speakerStatus\n$volumeText"
    }
    
    private fun updateDeviceInfo() {
        val deviceSummary = deviceInfoManager?.getDeviceSummary() ?: return
        
        textViewDeviceName?.text = "Device: ${deviceSummary.manufacturer} ${deviceSummary.model}"
        textViewDeviceModel?.text = "Model: ${deviceSummary.deviceName}"
        textViewAndroidVersion?.text = "Android: ${deviceSummary.androidVersion} (API ${deviceSummary.sdkVersion})"
        
        val totalMemMB = deviceSummary.totalMemory / (1024 * 1024)
        val availMemMB = deviceSummary.availableMemory / (1024 * 1024)
        textViewMemoryInfo?.text = "Memory: ${availMemMB}MB / ${totalMemMB}MB used"
    }
    
    override fun onPause() {
        super.onPause()
        updateJob?.cancel()
        updateJob = null
    }
    
    override fun onResume() {
        super.onResume()
        if (updateJob == null || updateJob?.isCancelled == true) {
            startUpdating()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        updateJob?.cancel()
        updateJob = null
        
        textViewCpuCores = null
        textViewCpuFreq = null
        textViewCpuTemp = null
        textViewBatteryLevel = null
        textViewBatteryStatus = null
        textViewBatteryTemp = null
        textViewBatteryVoltage = null
        textViewBatteryHealth = null
        textViewCameraInfo = null
        textViewAudioInfo = null
        textViewDeviceName = null
        textViewDeviceModel = null
        textViewAndroidVersion = null
        textViewMemoryInfo = null
    }
}
