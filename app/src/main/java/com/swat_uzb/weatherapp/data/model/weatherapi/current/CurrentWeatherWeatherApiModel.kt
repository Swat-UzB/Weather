package com.swat_uzb.weatherapp.data.model.weatherapi.current

import com.swat_uzb.weatherapp.data.model.weatherapi.forecast.Current

data class CurrentWeatherWeatherApiModel(
    val current: Current,
    val location: Location
)