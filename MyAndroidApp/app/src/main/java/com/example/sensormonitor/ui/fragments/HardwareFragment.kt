package com.example.sensormonitor.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sensormonitor.databinding.FragmentHardwareBinding
import com.example.sensormonitor.util.DeviceInfoManager

class HardwareFragment : Fragment() {

    private var _binding: FragmentHardwareBinding? = null
    private val binding get() = _binding!!

    private lateinit var deviceInfoManager: DeviceInfoManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHardwareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deviceInfoManager = DeviceInfoManager(requireContext())
        updateHardwareInfo()
    }

    private fun updateHardwareInfo() {
        binding.apply {
            tvCameraStatus.text = deviceInfoManager.getCameraStatus()
            tvAudioStatus.text = deviceInfoManager.getAudioStatus()
            
            val batteryInfo = deviceInfoManager.getBatteryInfo()
            tvBatteryInfo.text = "Level: ${batteryInfo.level}% | Temp: ${batteryInfo.temperature}°C"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
