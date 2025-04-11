package com.example.mqtt.model

// Модель данных для хранения показаний датчика DHT
data class SensorData(
    val temperature: Float,    // Температура в градусах Цельсия
    val humidity: Float,       // Влажность в процентах
    val timestamp: Long = System.currentTimeMillis() // Временная метка получения данных
)

// Модель данных для управления RGB светодиодом
data class RGBData(
    val red: Int,         // Значение красного цвета (0-255)
    val green: Int,       // Значение зеленого цвета (0-255)
    val blue: Int,        // Значение синего цвета (0-255)
    val brightness: Int   // Яркость (0-255)
) 