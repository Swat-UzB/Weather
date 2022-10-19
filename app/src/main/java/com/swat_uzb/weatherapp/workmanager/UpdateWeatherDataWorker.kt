package com.swat_uzb.weatherapp.workmanager

import android.app.NotificationManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.swat_uzb.weatherapp.domain.usecase.FetchForecastFromWeatherApi
import com.swat_uzb.weatherapp.domain.usecase.GetLocationsListUseCase
import com.swat_uzb.weatherapp.domain.usecase.UpdateLocationDataUseCase
import com.swat_uzb.weatherapp.ui.fragments.main.locationToString
import com.swat_uzb.weatherapp.ui.viewmodels.SharedViewModel
import com.swat_uzb.weatherapp.utils.sendNotification
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject


class UpdateWeatherDataWorker @AssistedInject constructor(
    private val sharedViewModel: SharedViewModel,
    private val notificationManager: NotificationManager,
    private val getLocationsListUseCase: GetLocationsListUseCase,
    private val fetchForecastFromWeatherApi: FetchForecastFromWeatherApi,
    private val updateLocationDataUseCase: UpdateLocationDataUseCase,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {


    override suspend fun doWork(): Result {
        getLocationsListUseCase.getLocationsList().onSuccess {
            if (it.isEmpty()) return Result.failure()
            it.forEach { currentUi ->
                fetchForecastFromWeatherApi.fetchForecastFromWeatherApi(currentUi.locationToString())
                    .onSuccess { data ->
                        updateLocationDataUseCase.updateLocationData(currentUi, data)
                    }.onFailure {
                        return Result.failure()
                    }
            }
            if (sharedViewModel.getNotification()) {
                getLocationsListUseCase.getLocationsList().onSuccess { newList ->
                    notificationManager.sendNotification(newList.first(), applicationContext)
                }
            }
        }
        return Result.success()
    }

    @AssistedFactory
    interface Factory {
        fun create(appContext: Context, params: WorkerParameters): UpdateWeatherDataWorker
    }


}