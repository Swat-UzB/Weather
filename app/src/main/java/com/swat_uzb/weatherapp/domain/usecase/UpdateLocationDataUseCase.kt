package com.swat_uzb.weatherapp.domain.usecase

import com.swat_uzb.weatherapp.data.model.mappers.toCurrentWeatherEntity
import com.swat_uzb.weatherapp.data.model.weatherapi.current.CurrentWeatherWeatherApiModel
import com.swat_uzb.weatherapp.data.model.weatherapi.forecast.ForecastData
import com.swat_uzb.weatherapp.domain.WeatherRepository
import javax.inject.Inject

class UpdateLocationDataUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend fun updateLocationData(currentId: Long, forecastData: ForecastData): Result<Unit> =
        kotlin.runCatching {
            with(weatherRepository) {
                deleteDailyForecast(currentId)
                deleteHourlyData(currentId)
                insertCurrentData(
                    forecastData.toCurrentWeatherEntity(
                        CurrentWeatherWeatherApiModel(
                            forecastData.current,
                            forecastData.location
                        ), id = currentId
                    )
                )
                insertHourlyData(forecastData, currentId)
                insertDailyForecast(forecastData, currentId)
            }
        }
}