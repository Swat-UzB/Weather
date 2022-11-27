package com.swat_uzb.weatherapp.domain.model


data class CurrentUi(
    val id: Long,
    val condition: String,
    val country: String,
    val daytime: Boolean,
    val feels_like: Int,
    val humidity: Int,
    val chance_of_rain: Int,
    val icon_url: String,
    val latitude: Double,
    val local_time: String,
    val location: String,
    val longitude: Double,
    val region: String,
    val temp: Int,
    val uv: Int,
    val wind_speed: String,
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    val current_location: Boolean
)
