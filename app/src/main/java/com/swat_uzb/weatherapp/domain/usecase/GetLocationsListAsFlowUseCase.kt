package com.swat_uzb.weatherapp.domain.usecase

import com.swat_uzb.weatherapp.domain.WeatherRepository
import javax.inject.Inject

class GetLocationsListAsFlowUseCase @Inject constructor(
    weatherRepository: WeatherRepository
) {
    val getLocationsList = weatherRepository.loadAllLocationsAsFlow()
}