package com.example.sensormonitor.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sensormonitor.R
import com.example.sensormonitor.databinding.FragmentSensorListBinding
import com.example.sensormonitor.model.SensorData
import com.example.sensormonitor.util.SensorDataManager

class SensorListFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentSensorListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sensorDataManager: SensorDataManager
    private lateinit var adapter: SensorDataAdapter
    private var sensorUpdateRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sensorDataManager = SensorDataManager(requireContext())
        setupRecyclerView()
        startSensorUpdates()
    }
    
    private fun setupRecyclerView() {
        adapter = SensorDataAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        
        // 定时刷新 UI (每 500ms)
        sensorUpdateRunnable = object : Runnable {
            override fun run() {
                updateSensorList()
                binding.recyclerView.postDelayed(this, 500)
            }
        }
        binding.recyclerView.post(sensorUpdateRunnable!!)
    }
    
    private fun updateSensorList() {
        val sensors = sensorDataManager.getAllAvailableSensors()
        val sensorDataList = sensors.map { sensor ->
            val data = sensorDataManager.getSensorData(sensor)
            SensorData(
                name = sensor.name,
                type = getSensorTypeName(sensor.type),
                vendor = sensor.vendor,
                values = data,
                unit = getSensorUnit(sensor.type)
            )
        }
        adapter.submitList(sensorDataList)
    }
    
    private fun startSensorUpdates() {
        val sensorManager = requireContext().getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        
        allSensors.forEach { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        // 实时数据由 SensorDataManager 处理
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        startSensorUpdates()
    }

    override fun onPause() {
        super.onPause()
        sensorUpdateRunnable?.let { binding.recyclerView.removeCallbacks(it) }
        val sensorManager = requireContext().getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun getSensorTypeName(type: Int): String {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER -> "加速度计"
            Sensor.TYPE_GYROSCOPE -> "陀螺仪"
            Sensor.TYPE_MAGNETIC_FIELD -> "磁力计"
            Sensor.TYPE_LIGHT -> "光线传感器"
            Sensor.TYPE_PROXIMITY -> "距离传感器"
            Sensor.TYPE_PRESSURE -> "气压计"
            Sensor.TYPE_TEMPERATURE -> "温度传感器"
            Sensor.TYPE_HUMIDITY -> "湿度传感器"
            Sensor.TYPE_GRAVITY -> "重力传感器"
            Sensor.TYPE_LINEAR_ACCELERATION -> "线性加速度"
            Sensor.TYPE_ROTATION_VECTOR -> "旋转矢量"
            Sensor.TYPE_SIGNIFICANT_MOTION -> "显著运动"
            Sensor.TYPE_STEP_DETECTOR -> "步数检测"
            Sensor.TYPE_STEP_COUNTER -> "计步器"
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> "地磁旋转矢量"
            Sensor.TYPE_HEART_RATE -> "心率"
            Sensor.TYPE_POSE_6DOF -> "6 自由度姿态"
            Sensor.TYPE_STATIONARY_DETECT -> "静止检测"
            Sensor.TYPE_MOTION_DETECT -> "运动检测"
            Sensor.TYPE_HEART_BEAT -> "心跳"
            Sensor.TYPE_DYNAMIC_HEAD_TRACKER -> "头部追踪"
            else -> "未知传感器 ($type)"
        }
    }
    
    private fun getSensorUnit(type: Int): String {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GRAVITY, Sensor.TYPE_LINEAR_ACCELERATION -> "m/s²"
            Sensor.TYPE_GYROSCOPE -> "rad/s"
            Sensor.TYPE_MAGNETIC_FIELD -> "μT"
            Sensor.TYPE_LIGHT -> "lux"
            Sensor.TYPE_PROXIMITY -> "cm"
            Sensor.TYPE_PRESSURE -> "hPa"
            Sensor.TYPE_TEMPERATURE -> "°C"
            Sensor.TYPE_HUMIDITY -> "%"
            Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> ""
            Sensor.TYPE_STEP_COUNTER, Sensor.TYPE_STEP_DETECTOR -> "steps"
            Sensor.TYPE_HEART_RATE, Sensor.TYPE_HEART_BEAT -> "bpm"
            else -> ""
        }
    }
}
