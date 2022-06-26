package com.swat_uzb.weatherapp.data.model.weatherapi.current

import com.google.gson.annotations.SerializedName

data class AirQuality(
    @SerializedName( "co") val carbonMonoxide: Double,
    @SerializedName ("gb-defra-index") val uk_defra_index: Int,
    @SerializedName ("no2") val nitrogenDioxide: Double,
    @SerializedName ("o3") val ozone: Double,
    @SerializedName ("pm10") val pm10: Double,
    @SerializedName ("pm2_5") val pm2_5: Double,
    @SerializedName("so2") val so2: Double,
    @SerializedName ("us-epa-index") val us_epa_index: Int
)