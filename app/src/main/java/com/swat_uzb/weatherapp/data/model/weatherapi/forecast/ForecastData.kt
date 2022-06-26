package com.swat_uzb.weatherapp.data.model.weatherapi.forecast

import com.swat_uzb.weatherapp.data.model.weatherapi.current.Location

data class ForecastData(
    val current: Current,
    val forecast: Forecast,
    val location: Location
)