package com.swat_uzb.weatherapp.domain.usecase

import com.swat_uzb.weatherapp.domain.WeatherRepository
import javax.inject.Inject


class GetSearchResultListUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend fun getSearchResultList(query: String) =
        repository.getSearchAutocompleteFromWeatherApi(query, "")
}