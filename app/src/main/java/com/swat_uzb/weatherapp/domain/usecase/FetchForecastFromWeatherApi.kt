package com.swat_uzb.weatherapp.domain.usecase

import com.swat_uzb.weatherapp.domain.WeatherRepository
import javax.inject.Inject

class FetchForecastFromWeatherApi @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend fun fetchForecastFromWeatherApi(location: String) =
        weatherRepository.getForecastFromWeatherApi(location)
}