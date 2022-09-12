package com.swat_uzb.weatherapp.domain.usecase

import com.swat_uzb.weatherapp.data.model.mappers.toCurrentWeatherEntity
import com.swat_uzb.weatherapp.data.model.weatherapi.current.CurrentWeatherWeatherApiModel
import com.swat_uzb.weatherapp.data.model.weatherapi.forecast.ForecastData
import com.swat_uzb.weatherapp.domain.WeatherRepository
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import javax.inject.Inject

class UpdateLocationDataUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend fun updateLocationData(current: CurrentUi, forecastData: ForecastData): Result<Unit> =
        kotlin.runCatching {
            with(weatherRepository) {
                deleteDailyForecast(current.id)
                deleteHourlyData(current.id)
                insertCurrentData(
                    forecastData.toCurrentWeatherEntity(
                        CurrentWeatherWeatherApiModel(
                            forecastData.current,
                            forecastData.location
                        ), id = current.id,current.current_location
                    )
                )
                insertHourlyData(forecastData, current.id)
                insertDailyForecast(forecastData, current.id)
            }
        }
}