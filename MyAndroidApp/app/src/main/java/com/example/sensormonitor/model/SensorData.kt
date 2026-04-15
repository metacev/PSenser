package com.example.sensormonitor.model

data class SensorData(
    val name: String,
    val unit: String,
    val values: List<String>,
    val timestamp: Long
)
