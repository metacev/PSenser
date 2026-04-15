package com.example.sensormonitor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sensormonitor.databinding.FragmentDeviceInfoBinding
import com.example.sensormonitor.util.DeviceInfoManager

class DeviceInfoFragment : Fragment() {

    private var _binding: FragmentDeviceInfoBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var deviceInfoManager: DeviceInfoManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        deviceInfoManager = DeviceInfoManager(requireContext())
        displayDeviceInfo()
    }
    
    private fun displayDeviceInfo() {
        // 基本信息
        binding.apply {
            textDeviceModel.text = deviceInfoManager.getDeviceModel()
            textAndroidVersion.text = deviceInfoManager.getAndroidVersion()
            textSdkVersion.text = "API ${deviceInfoManager.getSdkVersion()}"
            textManufacturer.text = deviceInfoManager.getManufacturer()
            textBrand.text = deviceInfoManager.getBrand()
            
            // 屏幕信息
            textScreenResolution.text = deviceInfoManager.getScreenResolution()
            textScreenDensity.text = "${deviceInfoManager.getScreenDensity()} dpi"
            
            // 内存信息
            textTotalMemory.text = deviceInfoManager.getTotalMemory()
            textAvailableMemory.text = deviceInfoManager.getAvailableMemory()
            
            // 存储信息
            textTotalStorage.text = deviceInfoManager.getTotalStorage()
            textAvailableStorage.text = deviceInfoManager.getAvailableStorage()
            
            // CPU 信息
            textCpuCores.text = "${deviceInfoManager.getCpuCoreCount()} 核心"
            textCpuArch.text = deviceInfoManager.getCpuArchitecture()
            
            // 其他信息
            textBuildNumber.text = deviceInfoManager.getBuildNumber()
            textSecurityPatch.text = deviceInfoManager.getSecurityPatch()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 刷新内存信息
        binding.apply {
            textAvailableMemory.text = deviceInfoManager.getAvailableMemory()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
