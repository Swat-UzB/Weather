package com.swat_uzb.weatherapp.domain.usecase

import com.swat_uzb.weatherapp.domain.WeatherRepository
import javax.inject.Inject

class LoadDailyByCurrentIdUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend fun getDailyList(currentId: Long) = repository.loadDailyForecast(currentId)
}