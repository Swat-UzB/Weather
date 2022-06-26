package com.swat_uzb.weatherapp.data.model.weatherapi.forecast

import com.google.gson.annotations.SerializedName

data class Forecast(
    @SerializedName(  "forecastday") val forecastDay: List<ForecastDay>
)