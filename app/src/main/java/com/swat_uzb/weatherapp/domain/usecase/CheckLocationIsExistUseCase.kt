package com.swat_uzb.weatherapp.domain.usecase

import com.swat_uzb.weatherapp.domain.WeatherRepository
import javax.inject.Inject

class CheckLocationIsExistUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend fun checkLocationIsExist(latitude: Double, longitude: Double):Result<Boolean> =
    kotlin.runCatching {
        weatherRepository.checkCurrent(latitude, longitude)
    }
}