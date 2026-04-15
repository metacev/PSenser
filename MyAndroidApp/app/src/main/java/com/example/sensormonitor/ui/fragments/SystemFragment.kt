package com.example.sensormonitor.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sensormonitor.databinding.FragmentSystemBinding
import com.example.sensormonitor.util.DeviceInfoManager

class SystemFragment : Fragment() {

    private var _binding: FragmentSystemBinding? = null
    private val binding get() = _binding!!

    private lateinit var deviceInfoManager: DeviceInfoManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSystemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deviceInfoManager = DeviceInfoManager(requireContext())
        updateSystemInfo()
    }

    private fun updateSystemInfo() {
        binding.apply {
            tvDeviceModel.text = deviceInfoManager.getDeviceModel()
            tvAndroidVersion.text = deviceInfoManager.getAndroidVersionName()
            tvRamInfo.text = deviceInfoManager.getRamInfo()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
