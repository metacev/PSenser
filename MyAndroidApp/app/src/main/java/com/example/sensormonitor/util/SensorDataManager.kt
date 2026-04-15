package com.example.sensormonitor.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sensormonitor.model.SensorData
import kotlin.math.roundToInt

class SensorDataManager(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _sensorData = MutableLiveData<Map<Int, SensorData>>()
    val sensorData: LiveData<Map<Int, SensorData>> = _sensorData

    private val availableSensors = mutableMapOf<Int, Sensor>()
    private val currentSensorValues = mutableMapOf<Int, SensorData>()

    fun startListening() {
        // Get all available sensors
        availableSensors.clear()
        sensorManager.getSensorList(Sensor.TYPE_ALL).forEach { sensor ->
            availableSensors[sensor.type] = sensor
        }

        // Register listeners for all available sensors
        availableSensors.forEach { (type, sensor) ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val type = event.sensor.type
        val values = event.values.clone()
        
        val data = when (type) {
            Sensor.TYPE_ACCELEROMETER -> SensorData(
                name = "Accelerometer",
                unit = "m/s²",
                values = listOf(
                    "X: ${values[0].roundToInt()}",
                    "Y: ${values[1].roundToInt()}",
                    "Z: ${values[2].roundToInt()}"
                ),
                timestamp = System.currentTimeMillis()
            )
            Sensor.TYPE_GYROSCOPE -> SensorData(
                name = "Gyroscope",
                unit = "rad/s",
                values = listOf(
                    "X: ${values[0].toFloat()}",
                    "Y: ${values[1].toFloat()}",
                    "Z: ${values[2].toFloat()}"
                ),
                timestamp = System.currentTimeMillis()
            )
            Sensor.TYPE_MAGNETIC_FIELD -> SensorData(
                name = "Magnetometer",
                unit = "μT",
                values = listOf(
                    "X: ${values[0].roundToInt()}",
                    "Y: ${values[1].roundToInt()}",
                    "Z: ${values[2].roundToInt()}"
                ),
                timestamp = System.currentTimeMillis()
            )
            Sensor.TYPE_PRESSURE -> SensorData(
                name = "Barometer",
                unit = "hPa",
                values = listOf("${values[0].toFloat()}"),
                timestamp = System.currentTimeMillis()
            )
            Sensor.TYPE_LIGHT -> SensorData(
                name = "Light Sensor",
                unit = "lux",
                values = listOf("${values[0].roundToInt()}"),
                timestamp = System.currentTimeMillis()
            )
            Sensor.TYPE_PROXIMITY -> SensorData(
                name = "Proximity",
                unit = "cm",
                values = listOf("${values[0].roundToInt()}"),
                timestamp = System.currentTimeMillis()
            )
            Sensor.TYPE_RELATIVE_HUMIDITY -> SensorData(
                name = "Humidity",
                unit = "%",
                values = listOf("${values[0].toFloat()}"),
                timestamp = System.currentTimeMillis()
            )
            Sensor.TYPE_AMBIENT_TEMPERATURE -> SensorData(
                name = "Temperature",
                unit = "°C",
                values = listOf("${values[0].toFloat()}"),
                timestamp = System.currentTimeMillis()
            )
            else -> {
                val sensorName = event.sensor.name
                SensorData(
                    name = sensorName,
                    unit = "",
                    values = values.map { it.toString() },
                    timestamp = System.currentTimeMillis()
                )
            }
        }

        currentSensorValues[type] = data
        _sensorData.value = currentSensorValues.toMap()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    fun getAvailableSensorCount(): Int = availableSensors.size
    
    fun getAvailableSensorNames(): List<String> {
        return availableSensors.values.map { it.name }.distinct()
    }
}
