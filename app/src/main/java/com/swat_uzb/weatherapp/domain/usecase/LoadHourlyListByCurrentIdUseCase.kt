package com.swat_uzb.weatherapp.domain.usecase

import com.swat_uzb.weatherapp.domain.WeatherRepository
import javax.inject.Inject

class LoadHourlyListByCurrentIdUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend fun getHourlyList(currentId: Long) = repository.loadHourlyData(currentId)
}