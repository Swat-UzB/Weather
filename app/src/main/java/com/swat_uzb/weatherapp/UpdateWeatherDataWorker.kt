package com.swat_uzb.weatherapp

import android.app.Notification
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.swat_uzb.weatherapp.WeatherService.Companion.NOTIFICATION_ID
import com.swat_uzb.weatherapp.domain.usecase.FetchForecastFromWeatherApi
import com.swat_uzb.weatherapp.domain.usecase.GetLocationsListUseCase
import com.swat_uzb.weatherapp.domain.usecase.UpdateLocationDataUseCase
import com.swat_uzb.weatherapp.ui.fragments.main.locationToString
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject


class UpdateWeatherDataWorker @AssistedInject constructor(
    private val getLocationsListUseCase: GetLocationsListUseCase,
    private val fetchForecastFromWeatherApi: FetchForecastFromWeatherApi,
    private val updateLocationDataUseCase: UpdateLocationDataUseCase,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }


    override suspend fun doWork(): Result {
        getLocationsListUseCase.getLocationsList().onSuccess {
            Log.d("TTTT", "listFromDb ${it.size}")
            if (it.isEmpty()) return Result.failure()
            it.forEach { currentUi ->
                fetchForecastFromWeatherApi.fetchForecastFromWeatherApi(currentUi.locationToString())
                    .onSuccess { data ->
                        Log.d("TTTT", "fetchData success")
                        updateLocationDataUseCase.updateLocationData(currentUi, data)
                    }.onSuccess {
                        Log.d("TTTT", "update data success")
                    }
                    .onFailure {
                        Log.d("TTTT", "update data failure")
                        return Result.failure()
                    }
            }
        }
        return Result.success()
    }

    private fun createNotification(): Notification {
        TODO()
    }

    @AssistedFactory
    interface Factory {
        fun create(appContext: Context, params: WorkerParameters): UpdateWeatherDataWorker
    }


}