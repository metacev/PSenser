package com.example.sensormonitor.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sensormonitor.databinding.FragmentSensorsBinding
import com.example.sensormonitor.util.SensorDataManager

class SensorsFragment : Fragment() {

    private var _binding: FragmentSensorsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorDataManager: SensorDataManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorDataManager = SensorDataManager(requireContext())

        sensorDataManager.sensorData.observe(viewLifecycleOwner) { dataMap ->
            updateSensorDisplay(dataMap)
        }

        binding.tvSensorCount.text = "Detecting sensors..."
    }

    private fun updateSensorDisplay(dataMap: Map<Int, Any>) {
        if (dataMap.isEmpty()) {
            binding.tvSensorCount.text = "No sensors detected"
            binding.sensorsContainer.removeAllViews()
            return
        }

        binding.tvSensorCount.text = "${dataMap.size} sensor(s) active"
        
        // Update UI with sensor data
        // This is a simplified version - in production you'd use RecyclerView
    }

    override fun onResume() {
        super.onResume()
        sensorDataManager.startListening()
    }

    override fun onPause() {
        super.onPause()
        sensorDataManager.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
