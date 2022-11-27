package com.swat_uzb.weatherapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import com.swat_uzb.weatherapp.ui.MainActivity

private const val NOTIFICATION_ID = 0xCA7

fun NotificationManager.sendNotification(currentUi: CurrentUi, applicationContext: Context) {

    createChannel(applicationContext)

    val contentIntent = Intent(applicationContext, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_IMMUTABLE
    )
    val drawable = currentUi.icon_url.getDrawable()
    val weatherIcon = BitmapFactory.decodeResource(
        applicationContext.resources, drawable
    )

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.channel_id)
    )

        .setSmallIcon(drawable)
        .setContentTitle("${currentUi.temp}° ${currentUi.region}")
        .setContentText(
            "${currentUi.condition} | ${
                applicationContext.getString(
                    R.string.feels_like,
                    currentUi.feels_like
                )
            }"
        )
        .setContentIntent(contentPendingIntent)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setLargeIcon(weatherIcon)
        .setOnlyAlertOnce(true)
        .setSound(null)
    notify(NOTIFICATION_ID, builder.build())
}


fun NotificationManager.cancelNotifications() {
    cancelAll()
}

fun NotificationManager.createChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            context.getString(R.string.channel_id),
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setShowBadge(false)
        }

        notificationChannel.apply {
            enableLights(true)
            lightColor = Color.RED
            description = context.getString(R.string.weather_notification_channel_description)
        }
        createNotificationChannel(notificationChannel)
    }
}