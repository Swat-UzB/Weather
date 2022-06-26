package com.swat_uzb.weatherapp.data.model.weatherapi.current

data class Location(
    val country: String,
    val lat: Double,
    val localtime: String,
    val localtime_epoch: String,
    val lon: Double,
    val name: String,
    val region: String,
    val tz_id: String
)