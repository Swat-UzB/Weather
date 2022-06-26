package com.swat_uzb.weatherapp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class WeatherWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        return Result.success()
    }
}