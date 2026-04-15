package com.example.sensormonitor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sensormonitor.R
import com.example.sensormonitor.model.SensorData
import com.example.sensormonitor.util.DeviceInfoManager
import com.example.sensormonitor.util.SensorDataManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Fragment to display detailed real-time sensor data with charts
 */
class SensorDetailFragment : Fragment() {
    
    private var sensorType: Int = 0
    private var sensorName: String = ""
    private var sensorDataManager: SensorDataManager? = null
    private var deviceInfoManager: DeviceInfoManager? = null
    
    private var textViewSensorName: TextView? = null
    private var textViewValues: TextView? = null
    private var textViewUnit: TextView? = null
    private var textViewTimestamp: TextView? = null
    private var textViewStatus: TextView? = null
    
    private var updateJob: Job? = null
    private var isListening = false
    
    companion object {
        private const val ARG_SENSOR_TYPE = "sensor_type"
        private const val ARG_SENSOR_NAME = "sensor_name"
        
        fun newInstance(sensorType: Int, sensorName: String): SensorDetailFragment {
            return SensorDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SENSOR_TYPE, sensorType)
                    putString(ARG_SENSOR_NAME, sensorName)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sensorType = it.getInt(ARG_SENSOR_TYPE, 0)
            sensorName = it.getString(ARG_SENSOR_NAME, "Unknown")
        }
        sensorDataManager = SensorDataManager(requireContext())
        deviceInfoManager = DeviceInfoManager(requireContext())
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sensor_detail, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        textViewSensorName = view.findViewById(R.id.textViewSensorName)
        textViewValues = view.findViewById(R.id.textViewValues)
        textViewUnit = view.findViewById(R.id.textViewUnit)
        textViewTimestamp = view.findViewById(R.id.textViewTimestamp)
        textViewStatus = view.findViewById(R.id.textViewStatus)
        
        textViewSensorName?.text = sensorName
        
        startListening()
    }
    
    private fun startListening() {
        if (isListening) return
        isListening = true
        
        updateJob = lifecycleScope.launch {
            var listener = sensorDataManager?.registerSensorListener(
                sensorType = sensorType,
                onSensorChanged = { data ->
                    updateUI(data)
                },
                onAccuracyChanged = { accuracy ->
                    updateAccuracy(accuracy)
                }
            )
            
            // Keep the coroutine active
            while (isActive) {
                delay(100)
            }
            
            // Cleanup when cancelled
            sensorDataManager?.unregisterListener(listener)
        }
    }
    
    private fun updateUI(data: SensorData) {
        activity?.runOnUiThread {
            textViewValues?.text = data.getValueString()
            textViewUnit?.text = data.unit
            textViewTimestamp?.text = "Updated: ${formatTimestamp(data.timestamp)}"
            textViewStatus?.text = "Status: Active"
        }
    }
    
    private fun updateAccuracy(accuracy: Int) {
        activity?.runOnUiThread {
            val accuracyText = when (accuracy) {
                3 -> "High Accuracy"
                2 -> "Medium Accuracy"
                1 -> "Low Accuracy"
                0 -> "Unreliable"
                else -> "Unknown"
            }
            textViewStatus?.text = "Accuracy: $accuracyText"
        }
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
    
    override fun onPause() {
        super.onPause()
        stopListening()
    }
    
    override fun onResume() {
        super.onResume()
        if (isVisible) {
            startListening()
        }
    }
    
    private fun stopListening() {
        isListening = false
        updateJob?.cancel()
        updateJob = null
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        stopListening()
        textViewSensorName = null
        textViewValues = null
        textViewUnit = null
        textViewTimestamp = null
        textViewStatus = null
    }
}
