package com.swat_uzb.weatherapp.workmanager

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.swat_uzb.weatherapp.UpdateWeatherDataWorker
import javax.inject.Inject

class SampleWorkerFactory @Inject constructor(
    private val updateWeatherDataWorker: UpdateWeatherDataWorker.Factory
):WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            UpdateWeatherDataWorker::class.java.name ->
                updateWeatherDataWorker.create(appContext, workerParameters)
            else -> null
        }
    }

}