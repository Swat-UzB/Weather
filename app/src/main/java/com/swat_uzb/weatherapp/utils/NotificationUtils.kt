package com.swat_uzb.weatherapp.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import com.swat_uzb.weatherapp.ui.MainActivity

private const val NOTIFICATION_ID = 0xCA7

fun NotificationManager.sendNotification(currentUi: CurrentUi, applicationContext: Context) {

    val contentIntent = Intent(applicationContext, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )

    val imageResource =
        applicationContext.resources.getIdentifier(currentUi.icon_url, null, Constants.PACKAGE_NAME)


    val weatherIcon = BitmapFactory.decodeResource(
        applicationContext.resources, imageResource
    )
//    val bitmap =
//        Bitmap.createScaledBitmap(weatherIcon, weatherIcon.width / 2, weatherIcon.height * 2, true)
    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.channel_id)
    )

        .setSmallIcon(imageResource)
        .setContentTitle("${currentUi.temp}° ${currentUi.region}")
        .setContentText("${currentUi.condition} Feels like ${currentUi.feels_like}°")
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