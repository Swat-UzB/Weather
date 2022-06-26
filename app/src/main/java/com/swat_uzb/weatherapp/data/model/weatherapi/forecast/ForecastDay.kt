package com.swat_uzb.weatherapp.data.model.weatherapi.forecast

import com.swat_uzb.weatherapp.data.model.weatherapi.astronomy.Astro

data class ForecastDay(
    val astro: Astro,
    val date: String,
    val date_epoch: Int,
    val day: Day,
    val hour: List<Hour>
)