package com.swat_uzb.weatherapp.domain.model


data class HourlyUi(
    val id: Long,
    val temp: Int,
    val icon_url: String,
    val date: String

)
