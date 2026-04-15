package com.example.sensormonitor.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.roundToInt

class SensorDataManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensorDataMap = mutableMapOf<Int, List<Float>>()

    fun getAllAvailableSensors(): List<Sensor> {
        return sensorManager.getSensorList(Sensor.TYPE_ALL)
    }

    fun getSensorData(sensor: Sensor): List<Float> {
        return sensorDataMap[sensor.type] ?: emptyList()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val values = it.values.toList()
            sensorDataMap[it.sensor.type] = values
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun formatValues(values: List<Float>): String {
        if (values.isEmpty()) return "无数据"
        return values.joinToString(" | ") { value ->
            "${value.roundToInt()}"
        }
    }
}
