package com.swat_uzb.weatherapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import com.swat_uzb.weatherapp.ui.MainActivity
import javax.inject.Inject

class WeatherService @Inject constructor() : Service() {
    private lateinit var currentUi: CurrentUi
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler


    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }


    override fun onCreate() {
        super.onCreate()
        notificationBuilder = startForegroundService()
        val handlerThread = HandlerThread("RouteTracking").apply {
            start()
        }
        serviceHandler = Handler(handlerThread.looper)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "weatherData"
        val channelName = "Weather Data"
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

    private fun getNotificationBuilder(pendingIntent: PendingIntent, channelId: String) =
        NotificationCompat.Builder(this, channelId)
            .setContentTitle("Agent approaching destination")
            .setContentText("Agent dispatched")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setTicker("Agent dispatched, tracking movement")


    private fun getPendingIntent() = PendingIntent.getActivity(
        this, 0,
        Intent(this, MainActivity::class.java), 0
    )

    private fun startForegroundService(): NotificationCompat.Builder {
        val pendingIntent = getPendingIntent()

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            ""
        }
        val notificationBuilder = getNotificationBuilder(pendingIntent, channelId)
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        return notificationBuilder
    }

    companion object {
        const val NOTIFICATION_ID = 0xCA7
        const val EXTRA_WEATHER_ID = "currentId"

    }
}