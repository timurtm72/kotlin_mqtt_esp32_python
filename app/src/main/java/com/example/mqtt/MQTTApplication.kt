package com.example.mqtt

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

class MQTTApplication : Application() {
    companion object {
        const val CHANNEL_ID = "mqtt_service"
        private const val TAG = "MQTTApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Инициализация приложения")
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MQTT Service"
            val descriptionText = "MQTT Service Notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Канал уведомлений создан")
        }
    }
}