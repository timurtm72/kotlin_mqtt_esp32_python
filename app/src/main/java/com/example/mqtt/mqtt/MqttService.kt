package com.example.mqtt.mqtt

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mqtt.MQTTApplication
import com.example.mqtt.R

class MqttService : Service() {
    private val TAG = "MqttService"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Сервис создан")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Сервис запущен")

        val notification = NotificationCompat.Builder(this, MQTTApplication.CHANNEL_ID)
            .setContentTitle("MQTT Service")
            .setContentText("Running")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Сервис уничтожен")
    }
}