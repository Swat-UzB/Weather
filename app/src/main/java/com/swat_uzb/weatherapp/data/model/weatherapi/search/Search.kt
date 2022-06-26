package com.swat_uzb.weatherapp.data.model.weatherapi.search

data class Search(
    val country: String,
    val id: Int,
    val lat: Double,
    val lon: Double,
    val name: String,
    val region: String,
    val url: String
)