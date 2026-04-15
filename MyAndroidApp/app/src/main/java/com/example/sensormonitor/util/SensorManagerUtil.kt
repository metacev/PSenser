package com.example.sensormonitor.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import com.example.sensormonitor.model.SensorData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Sensor Manager Utility - Handles all sensor operations
 */
class SensorManagerUtil(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val _sensorDataFlow = MutableStateFlow<Map<String, SensorData>>(emptyMap())
    val sensorDataFlow: StateFlow<Map<String, SensorData>> = _sensorDataFlow

    private val registeredSensors = mutableSetOf<Int>()
    private val sensorDataMap = mutableMapOf<String, SensorData>()

    // Get list of available sensors
    fun getAvailableSensors(): List<Sensor> {
        return sensorManager.getSensorList(Sensor.TYPE_ALL)
    }

    // Register for specific sensor
    fun registerSensor(sensorType: Int) {
        if (registeredSensors.contains(sensorType)) return
        
        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            registeredSensors.add(sensorType)
        }
    }

    // Register all available sensors
    fun registerAllSensors() {
        val sensors = getAvailableSensors()
        sensors.forEach { sensor ->
            registerSensor(sensor.type)
        }
    }

    // Unregister specific sensor
    fun unregisterSensor(sensorType: Int) {
        if (!registeredSensors.contains(sensorType)) return
        
        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor != null) {
            sensorManager.unregisterListener(this, sensor)
            registeredSensors.remove(sensorType)
        }
    }

    // Unregister all sensors
    fun unregisterAllSensors() {
        sensorManager.unregisterListener(this)
        registeredSensors.clear()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        
        val sensorName = event.sensor.name
        val sensorType = event.sensor.stringType
        
        val data = when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                SensorData(
                    name = "Accelerometer",
                    value = "X: ${event.values[0].format()} Y: ${event.values[1].format()} Z: ${event.values[2].format()}",
                    unit = "m/s²"
                )
            }
            Sensor.TYPE_GYROSCOPE -> {
                SensorData(
                    name = "Gyroscope",
                    value = "X: ${event.values[0].format()} Y: ${event.values[1].format()} Z: ${event.values[2].format()}",
                    unit = "rad/s"
                )
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                SensorData(
                    name = "Magnetometer",
                    value = "X: ${event.values[0].format()} Y: ${event.values[1].format()} Z: ${event.values[2].format()}",
                    unit = "μT"
                )
            }
            Sensor.TYPE_PRESSURE -> {
                SensorData(
                    name = "Barometer",
                    value = event.values[0].format(2),
                    unit = "hPa"
                )
            }
            Sensor.TYPE_LIGHT -> {
                SensorData(
                    name = "Light Sensor",
                    value = event.values[0].format(0),
                    unit = "lux"
                )
            }
            Sensor.TYPE_PROXIMITY -> {
                SensorData(
                    name = "Proximity",
                    value = event.values[0].format(2),
                    unit = "cm"
                )
            }
            Sensor.TYPE_RELATIVE_HUMIDITY -> {
                SensorData(
                    name = "Humidity",
                    value = event.values[0].format(1),
                    unit = "%"
                )
            }
            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                SensorData(
                    name = "Temperature",
                    value = event.values[0].format(1),
                    unit = "°C"
                )
            }
            Sensor.TYPE_GRAVITY -> {
                SensorData(
                    name = "Gravity",
                    value = "X: ${event.values[0].format()} Y: ${event.values[1].format()} Z: ${event.values[2].format()}",
                    unit = "m/s²"
                )
            }
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                SensorData(
                    name = "Linear Acceleration",
                    value = "X: ${event.values[0].format()} Y: ${event.values[1].format()} Z: ${event.values[2].format()}",
                    unit = "m/s²"
                )
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorData(
                    name = "Rotation Vector",
                    value = "X: ${event.values[0].format(3)} Y: ${event.values[1].format(3)} Z: ${event.values[2].format(3)}",
                    unit = ""
                )
            }
            Sensor.TYPE_STEP_COUNTER -> {
                SensorData(
                    name = "Step Counter",
                    value = event.values[0].toLong().toString(),
                    unit = "steps"
                )
            }
            Sensor.TYPE_SIGNIFICANT_MOTION -> {
                SensorData(
                    name = "Significant Motion",
                    value = if (event.values[0] > 0) "Detected" else "None",
                    unit = ""
                )
            }
            else -> {
                SensorData(
                    name = sensorName,
                    value = event.values.joinToString(", ") { it.format(2) },
                    unit = ""
                )
            }
        }

        sensorDataMap[sensorType] = data
        _sensorDataFlow.value = sensorDataMap.toMap()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    private fun Float.format(decimalPlaces: Int = 2): String {
        return if (decimalPlaces == 0) {
            this.toInt().toString()
        } else {
            String.format("%.${decimalPlaces}f", this)
        }
    }

    fun release() {
        unregisterAllSensors()
    }
}
