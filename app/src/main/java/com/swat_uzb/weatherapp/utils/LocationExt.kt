package com.swat_uzb.weatherapp.utils

import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER

fun LocationManager.isGpsOn() =
    isProviderEnabled(GPS_PROVIDER) || isProviderEnabled(NETWORK_PROVIDER)