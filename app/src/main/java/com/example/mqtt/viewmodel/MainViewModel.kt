package com.example.mqtt.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mqtt.model.RGBData
import com.example.mqtt.model.SensorData
import com.example.mqtt.mqtt.MQTTClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.LinkedList

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MainViewModel"

    // MQTT клиент для взаимодействия с брокером
    private val mqttClient = MQTTClient(application.applicationContext)

    // StateFlow для хранения истории показаний датчика
    private val _sensorData = MutableStateFlow<List<SensorData>>(emptyList())
    val sensorData: StateFlow<List<SensorData>> = _sensorData.asStateFlow()

    // StateFlow для отслеживания состояния подключения
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Буфер для хранения последних показаний
    private val dataHistory = LinkedList<SensorData>()
    private val maxDataPoints = 20 // Максимальное количество точек на графике

    init {
        Log.d(TAG, "Инициализация ViewModel")
        connectToMQTT()
    }

    // Подключение к MQTT брокеру
    private fun connectToMQTT() {
        Log.d(TAG, "connectToMQTT: Попытка подключения к брокеру")
        viewModelScope.launch {
            try {
                val connected = mqttClient.connect()
                Log.d(TAG, "connectToMQTT: Статус подключения: $connected")
                _isConnected.value = connected

                if (connected) {
                    Log.d(TAG, "connectToMQTT: Подключение успешно, подписываемся на топики")
                    subscribeToDHT()
                } else {
                    Log.e(TAG, "connectToMQTT: Не удалось подключиться к брокеру")
                }
            } catch (e: Exception) {
                Log.e(TAG, "connectToMQTT: Ошибка при подключении", e)
            }
        }
    }

    // Подписка на топик с данными датчика
    private fun subscribeToDHT() {
        Log.d(TAG, "subscribeToDHT: Подписка на топик DHT")
        try {
            mqttClient.subscribe("esp32/sensor/dht") { message ->
                Log.d(TAG, "Получено сообщение: $message")
                try {
                    val json = JSONObject(message)
                    val sensorData = SensorData(
                        temperature = json.getDouble("temperature").toFloat(),
                        humidity = json.getDouble("humidity").toFloat()
                    )
                    Log.d(TAG, "Обработанные данные: temp=${sensorData.temperature}, hum=${sensorData.humidity}")

                    dataHistory.add(sensorData)
                    if (dataHistory.size > maxDataPoints) {
                        dataHistory.removeFirst()
                    }

                    _sensorData.value = dataHistory.toList()
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при обработке сообщения: $message", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "subscribeToDHT: Ошибка при подписке", e)
        }
    }

    // Отправка значений RGB на ESP32
    fun sendRGBValues(rgbData: RGBData) {
        Log.d(TAG, "sendRGBValues: Отправка RGB данных: $rgbData")
        try {
            val json = JSONObject().apply {
                put("red", rgbData.red)
                put("green", rgbData.green)
                put("blue", rgbData.blue)
                put("brightness", rgbData.brightness)
            }

            mqttClient.publish("esp32/control/rgb", json.toString())
            Log.d(TAG, "sendRGBValues: Данные успешно отправлены")
        } catch (e: Exception) {
            Log.e(TAG, "sendRGBValues: Ошибка при отправке данных", e)
        }
    }

    // Очистка ресурсов при уничтожении ViewModel
    override fun onCleared() {
        Log.d(TAG, "onCleared: Очистка ViewModel")
        try {
            mqttClient.disconnect()
            Log.d(TAG, "onCleared: MQTT клиент отключен")
        } catch (e: Exception) {
            Log.e(TAG, "onCleared: Ошибка при отключении", e)
        }
        super.onCleared()
    }
}