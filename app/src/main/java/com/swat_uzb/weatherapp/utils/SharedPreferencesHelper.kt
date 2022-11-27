package com.swat_uzb.weatherapp.utils

import android.app.Application
import android.content.SharedPreferences
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.utils.Constants.DEFAULT_AUTO_REFRESH
import com.swat_uzb.weatherapp.utils.Constants.DEFAULT_TEMPERATURE
import com.swat_uzb.weatherapp.utils.Constants.DEFAULT_WIND_SPEED
import com.swat_uzb.weatherapp.utils.Constants.NOT_SET
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesHelper @Inject constructor(
    private val context: Application,
    private val prefs: SharedPreferences
) {
    private val editor = prefs.edit()

    fun isRefreshOnLaunchOn() = prefs.getBoolean(context.getString(R.string.key_refresh_on_launch), false)


    fun getAutoRefresh() = prefs.getString(
        context.getString(R.string.key_auto_refresh),
        DEFAULT_AUTO_REFRESH
    )

    fun getNotification() = prefs.getBoolean(context.getString(R.string.key_notification), true)

    fun getWindSpeed() = prefs.getString(
        context.getString(R.string.key_wind_speed),
        DEFAULT_WIND_SPEED
    ) == DEFAULT_WIND_SPEED

    fun getTemperature() = prefs.getString(
        context.getString(R.string.key_temperature),
        context.resources.getStringArray(R.array.temperature_entries)[0]
    ) == DEFAULT_TEMPERATURE

    fun getLocation() = prefs.getString(
        context.getString(R.string.key_location),
        NOT_SET
    )

    fun setLocation(int: Int) =
        editor.putString(
            context.getString(R.string.key_location),
            context.resources.getStringArray(R.array.location_values)[int]
        ).apply()


    fun clearLocationData() {
        editor.remove(context.getString(R.string.key_location)).apply()
    }

    // current location added or not
    fun getCurrent() = prefs.getBoolean(
        context.getString(R.string.key_current_location), false
    )

    fun setCurrent(boolean: Boolean) =
        editor.putBoolean(
            context.getString(R.string.key_current_location),
            boolean
        ).apply()


    fun getIsFirstTime() = prefs.getBoolean(
        context.getString(R.string.key_is_first_time), true
    )

    fun setIsFistTime(boolean: Boolean) {
        editor.putBoolean(
            context.getString(R.string.key_is_first_time),
            boolean
        )
        editor.apply()
    }
}
