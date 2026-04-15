package com.example.sensormonitor.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.sensormonitor.model.SensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Utility class for managing sensor data collection
 */
class SensorDataManager(private val context: Context) {
    
    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    
    /**
     * Get list of all available sensors on the device
     */
    fun getAvailableSensors(): List<Sensor> {
        return sensorManager.getSensorList(Sensor.TYPE_ALL)
    }
    
    /**
     * Get human-readable sensor type name
     */
    fun getSensorTypeName(type: Int): String {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
            Sensor.TYPE_MAGNETIC_FIELD -> "Magnetometer"
            Sensor.TYPE_GYROSCOPE -> "Gyroscope"
            Sensor.TYPE_LIGHT -> "Light Sensor"
            Sensor.TYPE_PROXIMITY -> "Proximity Sensor"
            Sensor.TYPE_GRAVITY -> "Gravity"
            Sensor.TYPE_LINEAR_ACCELERATION -> "Linear Acceleration"
            Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
            Sensor.TYPE_RELATIVE_HUMIDITY -> "Humidity"
            Sensor.TYPE_AMBIENT_TEMPERATURE -> "Ambient Temperature"
            Sensor.TYPE_PRESSURE -> "Barometer"
            Sensor.TYPE_SIGNIFICANT_MOTION -> "Significant Motion"
            Sensor.TYPE_STEP_DETECTOR -> "Step Detector"
            Sensor.TYPE_STEP_COUNTER -> "Step Counter"
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> "Geomagnetic Rotation"
            Sensor.TYPE_HEART_RATE -> "Heart Rate"
            Sensor.TYPE_TILT_DETECTOR -> "Tilt Detector"
            Sensor.TYPE_WAKE_GESTURE -> "Wake Gesture"
            Sensor.TYPE_GLANCE_GESTURE -> "Glance Gesture"
            Sensor.TYPE_PICK_UP_GESTURE -> "Pick Up Gesture"
            Sensor.TYPE_WRIST_TILT_GESTURE -> "Wrist Tilt"
            Sensor.TYPE_DEVICE_ORIENTATION -> "Device Orientation"
            Sensor.TYPE_POSE_6DOF -> "Pose 6DOF"
            Sensor.TYPE_STATIONARY_DETECT -> "Stationary Detect"
            Sensor.TYPE_MOTION_DETECT -> "Motion Detect"
            Sensor.TYPE_HEART_BEAT -> "Heart Beat"
            Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT -> "Off-body Detect"
            Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> "Accelerometer Uncalibrated"
            Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> "Gyroscope Uncalibrated"
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> "Magnetometer Uncalibrated"
            Sensor.TYPE_GAME_ROTATION_VECTOR -> "Game Rotation Vector"
            Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
            else -> "Unknown Sensor ($type)"
        }
    }
    
    /**
     * Get unit for sensor type
     */
    fun getSensorUnit(type: Int): String {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_GRAVITY -> "m/s²"
            Sensor.TYPE_MAGNETIC_FIELD -> "μT"
            Sensor.TYPE_GYROSCOPE -> "rad/s"
            Sensor.TYPE_LIGHT -> "lux"
            Sensor.TYPE_PROXIMITY -> "cm"
            Sensor.TYPE_PRESSURE -> "hPa"
            Sensor.TYPE_RELATIVE_HUMIDITY -> "%"
            Sensor.TYPE_AMBIENT_TEMPERATURE -> "°C"
            Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GAME_ROTATION_VECTOR -> ""
            else -> ""
        }
    }
    
    /**
     * Create a Flow to observe sensor data in real-time
     */
    fun observeSensorData(sensorType: Int): Flow<SensorData> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        
        if (sensor == null) {
            close()
            return@callbackFlow
        }
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(
                    SensorData(
                        sensorType = event.sensor.type,
                        sensorName = getSensorTypeName(event.sensor.type),
                        values = event.values.clone(),
                        timestamp = System.currentTimeMillis(),
                        unit = getSensorUnit(event.sensor.type)
                    )
                )
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Handle accuracy changes if needed
            }
        }
        
        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    /**
     * Register sensor listener with custom callback
     */
    fun registerSensorListener(
        sensorType: Int,
        onSensorChanged: (SensorData) -> Unit,
        onAccuracyChanged: (Int) -> Unit = {}
    ): SensorEventListener? {
        val sensor = sensorManager.getDefaultSensor(sensorType) ?: return null
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                onSensorChanged(
                    SensorData(
                        sensorType = event.sensor.type,
                        sensorName = getSensorTypeName(event.sensor.type),
                        values = event.values.clone(),
                        timestamp = System.currentTimeMillis(),
                        unit = getSensorUnit(event.sensor.type)
                    )
                )
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                onAccuracyChanged(accuracy)
            }
        }
        
        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        
        return listener
    }
    
    /**
     * Unregister sensor listener
     */
    fun unregisterListener(listener: SensorEventListener?) {
        listener?.let {
            sensorManager.unregisterListener(it)
        }
    }
}
