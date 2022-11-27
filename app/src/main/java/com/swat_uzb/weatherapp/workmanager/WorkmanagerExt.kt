package com.swat_uzb.weatherapp.workmanager

import androidx.work.*
import com.swat_uzb.weatherapp.utils.Constants
import java.util.concurrent.TimeUnit

fun WorkManager.startWorkManager(intervalTime: Long) {
        val weatherConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val updateWeatherWorkerRequest =
            PeriodicWorkRequestBuilder<UpdateWeatherDataWorker>(intervalTime, TimeUnit.HOURS)
                .setConstraints(weatherConstraints)
                .setInitialDelay(intervalTime / 2, TimeUnit.HOURS)
                .build()

        if (intervalTime == 0L) {
            cancelUniqueWork(Constants.TAG_WORKER)
        } else {
            enqueueUniquePeriodicWork(
                Constants.TAG_WORKER,
                ExistingPeriodicWorkPolicy.REPLACE,
                updateWeatherWorkerRequest
            )
        }

    }
