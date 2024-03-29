package com.swat_uzb.weatherapp.domain.usecase

import android.util.Log
import com.swat_uzb.weatherapp.data.model.mappers.toCurrentWeatherEntity
import com.swat_uzb.weatherapp.data.model.weatherapi.current.CurrentWeatherWeatherApiModel
import com.swat_uzb.weatherapp.data.model.weatherapi.forecast.ForecastData
import com.swat_uzb.weatherapp.domain.WeatherRepository
import javax.inject.Inject

class SaveNewLocationUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend fun addNewLocation(
        forecastData: ForecastData,
        id: Long,
        isCurrent: Boolean
    ): Result<Unit> =
        kotlin.runCatching {
            Log.d("TTTT","AddNewLocation saveNewLocation $isCurrent")
            val currentId: Long = weatherRepository.insertCurrentData(
                forecastData.toCurrentWeatherEntity(
                    CurrentWeatherWeatherApiModel(forecastData.current, forecastData.location),
                    id,
                    isCurrent
                )
            )
            weatherRepository.insertHourlyData(forecastData, currentId)
            weatherRepository.insertDailyForecast(forecastData, currentId)
        }
}