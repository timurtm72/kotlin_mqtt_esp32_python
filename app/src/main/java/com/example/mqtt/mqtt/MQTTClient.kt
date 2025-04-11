package com.example.mqtt.mqtt

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val MQTT_BROKER = "193.43.147.210"
private const val MQTT_PORT = 1883
private const val MQTT_USERNAME = "timur"
private const val MQTT_PASSWORD = "timur_1972"

class MQTTClient(
    private val context: Context,
    private val serverUri: String = "tcp://$MQTT_BROKER:$MQTT_PORT",
    private val clientId: String = MqttClient.generateClientId()
) {
    private val TAG = "MQTTClient"
    private val mqttClient = MqttClient(serverUri, clientId, null)
    private var isConnected = false

    init {
        Log.d(TAG, "Инициализация MQTT клиента: serverUri=$serverUri, clientId=$clientId")
        startMqttService()
    }

    private fun startMqttService() {
        val serviceIntent = Intent(context, MqttService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    suspend fun connect(): Boolean = suspendCoroutine { continuation ->
        try {
            Log.d(TAG, "connect: Попытка подключения к $serverUri")

            val mqttConnectOptions = MqttConnectOptions().apply {
                userName = MQTT_USERNAME
                password = MQTT_PASSWORD.toCharArray()
                isCleanSession = true
            }

            mqttClient.connect(mqttConnectOptions)
            isConnected = true
            continuation.resume(true)
        } catch (e: Exception) {
            Log.e(TAG, "connect: Исключение при подключении", e)
            isConnected = false
            continuation.resume(false)
        }
    }

    fun subscribe(topic: String, qos: Int = 1, onMessageReceived: (String) -> Unit) {
        try {
            Log.d(TAG, "subscribe: Подписка на топик $topic")

            mqttClient.subscribe(topic, qos)
            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e(TAG, "connectionLost: Соединение потеряно", cause)
                    isConnected = false
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    message?.payload?.let {
                        val messageText = String(it)
                        Log.d(TAG, "messageArrived: Получено сообщение в топике $topic: $messageText")
                        onMessageReceived(messageText)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d(TAG, "deliveryComplete: Сообщение доставлено")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "subscribe: Ошибка при подписке", e)
        }
    }

    fun publish(topic: String, message: String, qos: Int = 1) {
        try {
            val mqttMessage = MqttMessage().apply {
                payload = message.toByteArray()
                this.qos = qos
            }

            mqttClient.publish(topic, mqttMessage)
            Log.d(TAG, "Сообщение успешно отправлено в топик $topic")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при публикации: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            mqttClient.disconnect()
            isConnected = false
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при отключении: ${e.message}")
        }
    }

    fun isConnected() = isConnected
}