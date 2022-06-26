package com.swat_uzb.weatherapp.domain.model

data class DailyUi(
    val id: Long,
    val chance_of_rain: Int,
    val condition: String,
    val date: String,
    val icon_url: String,
    val max_temp: Int,
    val min_temp: Int,
    val day_of_week: String
)
