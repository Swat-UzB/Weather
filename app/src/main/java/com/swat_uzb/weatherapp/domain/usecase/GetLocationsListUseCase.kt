package com.swat_uzb.weatherapp.domain.usecase

import com.swat_uzb.weatherapp.domain.WeatherRepository
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import javax.inject.Inject

class GetLocationsListUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend fun getLocationsList(): Result<List<CurrentUi>> = kotlin.runCatching {
        weatherRepository.loadAllLocations()
    }
}