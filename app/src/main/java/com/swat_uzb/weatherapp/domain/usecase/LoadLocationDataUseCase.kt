package com.swat_uzb.weatherapp.domain.usecase

import com.swat_uzb.weatherapp.domain.WeatherRepository
import javax.inject.Inject

class LoadLocationDataUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend fun loadLocationData(long: Long) = weatherRepository.loadCurrentWeather(long)
}
