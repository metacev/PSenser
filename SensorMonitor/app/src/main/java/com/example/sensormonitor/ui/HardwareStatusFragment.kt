package com.example.sensormonitor.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sensormonitor.databinding.FragmentHardwareStatusBinding
import com.example.sensormonitor.util.HardwareStatusManager

class HardwareStatusFragment : Fragment() {

    private var _binding: FragmentHardwareStatusBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var hardwareStatusManager: HardwareStatusManager
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHardwareStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        hardwareStatusManager = HardwareStatusManager(requireContext())
        startUpdates()
    }
    
    private fun startUpdates() {
        updateRunnable = object : Runnable {
            override fun run() {
                updateHardwareStatus()
                handler.postDelayed(this, 1000) // 每秒更新
            }
        }
        handler.post(updateRunnable!!)
    }
    
    private fun updateHardwareStatus() {
        binding.apply {
            // 电池信息
            cardBattery.apply {
                textBatteryLevel.text = hardwareStatusManager.getBatteryLevel()
                textBatteryStatus.text = hardwareStatusManager.getBatteryStatus()
                textBatteryHealth.text = hardwareStatusManager.getBatteryHealth()
                textBatteryTemp.text = hardwareStatusManager.getBatteryTemperature()
                textBatteryVoltage.text = hardwareStatusManager.getBatteryVoltage()
                progressBattery.progress = hardwareStatusManager.getBatteryLevelPercent()
            }
            
            // CPU 状态
            cardCpu.apply {
                textCpuUsage.text = hardwareStatusManager.getCpuUsage()
                textCpuFreq.text = hardwareStatusManager.getCpuFrequency()
                textCpuTemp.text = hardwareStatusManager.getCpuTemperature()
            }
            
            // 相机状态
            cardCamera.apply {
                textCameraFront.text = hardwareStatusManager.getFrontCameraStatus()
                textCameraBack.text = hardwareStatusManager.getBackCameraStatus()
                textCameraCount.text = "${hardwareStatusManager.getCameraCount()} 摄像头"
            }
            
            // 音频状态
            cardAudio.apply {
                textMicStatus.text = hardwareStatusManager.getMicrophoneStatus()
                textSpeakerStatus.text = hardwareStatusManager.getSpeakerStatus()
                textVolumeLevel.text = hardwareStatusManager.getVolumeLevel()
            }
            
            // 网络连接
            cardNetwork.apply {
                textWifiStatus.text = hardwareStatusManager.getWifiStatus()
                textMobileDataStatus.text = hardwareStatusManager.getMobileDataStatus()
                textIpAddress.text = hardwareStatusManager.getIpAddress()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startUpdates()
    }

    override fun onPause() {
        super.onPause()
        updateRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
