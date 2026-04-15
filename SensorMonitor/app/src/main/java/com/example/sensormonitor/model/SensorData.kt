package com.example.sensormonitor.model

data class SensorData(
    val name: String,
    val type: String,
    val vendor: String,
    val values: List<Float>,
    val unit: String
)
