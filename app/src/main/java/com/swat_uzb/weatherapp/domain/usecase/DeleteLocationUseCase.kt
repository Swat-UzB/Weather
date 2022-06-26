package com.swat_uzb.weatherapp.domain.usecase

import com.swat_uzb.weatherapp.domain.WeatherRepository
import javax.inject.Inject


class DeleteLocationUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend fun deleteLocation(currentId: Long): Result<Unit> =
        kotlin.runCatching { weatherRepository.deleteCurrentData(currentId) }
}