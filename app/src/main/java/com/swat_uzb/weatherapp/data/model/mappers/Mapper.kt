package com.swat_uzb.weatherapp.data.model.mappers

import android.annotation.SuppressLint
import com.swat_uzb.weatherapp.data.database.CurrentWeatherEntity
import com.swat_uzb.weatherapp.data.database.DailyForecastEntity
import com.swat_uzb.weatherapp.data.database.HourlyDataEntity
import com.swat_uzb.weatherapp.data.model.weatherapi.current.CurrentWeatherWeatherApiModel
import com.swat_uzb.weatherapp.data.model.weatherapi.forecast.Day
import com.swat_uzb.weatherapp.data.model.weatherapi.forecast.ForecastData
import com.swat_uzb.weatherapp.data.model.weatherapi.forecast.Hour
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import com.swat_uzb.weatherapp.domain.model.DailyUi
import com.swat_uzb.weatherapp.domain.model.HourlyUi
import com.swat_uzb.weatherapp.utils.Constants.TIME_CURRENT_FORMAT
import com.swat_uzb.weatherapp.utils.Constants.TIME_STRING_FORMAT
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

const val TIME_FORMAT = "yyyy-MM-dd"


fun ForecastData.toCurrentWeatherEntity(
    currentWeather: CurrentWeatherWeatherApiModel,
    id: Long,
    isCurrent: Boolean = false
) =
    CurrentWeatherEntity(
        id = id,
        condition = currentWeather.current.condition.text,
        country = currentWeather.location.country,
        daytime = currentWeather.current.is_day == 1,
        feels_like_c = currentWeather.current.feelslike_c.roundToInt(),
        feels_like_f = currentWeather.current.feelslike_f.roundToInt(),
        humidity = currentWeather.current.humidity,
        icon_url = currentWeather.current.condition.icon,
        latitude = currentWeather.location.lat,
        local_time = getCurrentDateWithHour(),
        location = currentWeather.location.name,
        longitude = currentWeather.location.lon,
        region = currentWeather.location.region,
        temp_c = currentWeather.current.temp_c.roundToInt(),
        temp_f = currentWeather.current.temp_f.roundToInt(),
        uv = currentWeather.current.uv,
        wind_degree = currentWeather.current.wind_degree,
        wind_direction = currentWeather.current.wind_dir,
        wind_kph = currentWeather.current.wind_kph.roundToInt(),
        wind_mph = currentWeather.current.wind_mph.roundToInt(),
        moonrise = forecast.forecastDay[0].astro.moonrise,
        moonset = forecast.forecastDay[0].astro.moonset,
        sunrise = forecast.forecastDay[0].astro.sunrise,
        sunset = forecast.forecastDay[0].astro.sunset,
        current_location = isCurrent
    )

fun ForecastData.toHourlyDataEntity(hour: Hour, currentId: Long) = HourlyDataEntity(
    chance_of_rain = hour.chance_of_rain,
    chance_of_snow = hour.chance_of_snow,
    cloud = hour.cloud,
    condition = hour.condition.text,
    icon_url = hour.condition.icon,
    feels_like_c = hour.feelslike_c.roundToInt(),
    feels_like_f = hour.feelslike_f.roundToInt(),
    humidity = hour.humidity,
    is_day = hour.is_day,
    pressure_in = hour.pressure_in,
    pressure_mb = hour.pressure_mb,
    temp_c = hour.temp_c.roundToInt(),
    temp_f = hour.temp_f.roundToInt(),
    time = hour.time,
    time_epoch = hour.time_epoch,
    uv = hour.uv,
    vis_km = hour.vis_km,
    vis_miles = hour.vis_miles,
    will_it_rain = hour.will_it_rain,
    will_it_snow = hour.will_it_snow,
    wind_degree = hour.wind_degree,
    wind_dir = hour.wind_dir,
    wind_kph = hour.wind_kph.roundToInt(),
    wind_mph = hour.wind_mph.roundToInt(),
    currentId = currentId

)

fun ForecastData.toDailyForecastEntity(day: Day, currentId: Long, date: String) =
    DailyForecastEntity(
        chance_of_rain = day.daily_chance_of_rain,
        condition = day.condition.text,
        date = date,
        icon_url = day.condition.icon,
        max_temp_c = day.maxtemp_c.roundToInt(),
        max_temp_f = day.maxtemp_f.roundToInt(),
        min_temp_c = day.mintemp_c.roundToInt(),
        min_temp_f = day.mintemp_f.roundToInt(),
        currentId = currentId
    )

private fun convertDateToDayOfWeek(date: String): String {
    if (date == getCurrentDate()) {
        return "Today"
    }
    val day = date.takeLast(2).toInt()
    val yearLastTwo = date.substring(2, 4).toInt()
    val month = date.substring(5, 7).toInt()
    val yearCode = (6 + yearLastTwo + yearLastTwo / 4) % 7
    val monthCode = when (month) {
        1, 10 -> 1
        12, 9 -> 6
        5 -> 2
        8 -> 3
        2, 3, 11 -> 4
        6 -> 5
        4, 7 -> 0
        else -> 10
    }

    var result = (day + monthCode + yearCode) % 7
    if (yearLastTwo % 4 == 0 && (month == 1 || month == 2)) result -= 1
    return when (result) {
        0 -> "Saturday"
        1 -> "Sunday"
        2 -> "Monday"
        3 -> "Tuesday"
        4 -> "Wednesday"
        5 -> "Thursday"
        6 -> "Friday"
        else -> "Unknown"
    }
}

private fun bindDate(date: String) = when (
    date.substring(5, 7).toInt()) {
    1 -> "January"
    2 -> "February"
    3 -> "March"
    4 -> "April"
    5 -> "May"
    6 -> "June"
    7 -> "July"
    8 -> "August"
    9 -> "September"
    10 -> "October"
    11 -> "November"
    else -> "December"
}.plus(" " + date.drop(8).toInt())

private fun convertTime(time: String): String {
    val a = time.takeLast(2)
    var hh = time.dropLast(6)
    val mm = time.substring(3, 5)
    return if (a == "AM") {
        if (hh == "12") hh = "00"
        "$hh:$mm"
    } else {
        if (hh != "12") hh = (hh.toInt() + 12).toString()
        "$hh:$mm"
    }
}

private fun isNumeric(toCheck: String): String {
    return if (toCheck.trim()[0].isDigit()) {
        convertTime(toCheck)
    } else toCheck
}

private fun getCorrect(boolean: Boolean, case1: Int, case2: Int) =
    case1.takeIf { boolean } ?: case2

private fun getUrl(str: String): String {
    str.substring(str.indexOf("4/") + 2, str.lastIndexOf(".")).apply {
        return "@drawable/_${drop(indexOf("/") + 2)}_${take(indexOf("/"))}"
    }
}

@SuppressLint("SimpleDateFormat")
private fun getCurrentDate() = SimpleDateFormat(TIME_FORMAT).format(Date())

@SuppressLint("SimpleDateFormat")
private fun getCurrentDateWithHour() = SimpleDateFormat(TIME_CURRENT_FORMAT).format(Date())

fun CurrentWeatherEntity.toCurrentUi(isSelsiy: Boolean, isMps: Boolean) = CurrentUi(
    id,
    condition,
    country,
    daytime,
    getCorrect(isSelsiy, feels_like_c, feels_like_f),
    humidity,
    getUrl(icon_url),
    latitude,
    local_time,
    location,
    longitude,
    region,
    getCorrect(isSelsiy, temp_c, temp_f),
    uv.roundToInt(),
    wind_mph.toString().plus(" m/s").takeIf { isMps }
        ?: wind_kph.toString().plus(" km/s"),
    isNumeric(sunrise),
    isNumeric(sunset),
    isNumeric(moonrise),
    isNumeric(moonset),
    current_location
)

fun DailyForecastEntity.toDailyUi(isSelsiy: Boolean) = DailyUi(
    id,
    chance_of_rain.toInt(),
    condition,
    bindDate(date),
    getUrl(icon_url),
    getCorrect(isSelsiy, max_temp_c, max_temp_f),
    getCorrect(isSelsiy, min_temp_c, min_temp_f),
    convertDateToDayOfWeek(date)
)

fun HourlyDataEntity.toHourlyUi(isSelsiy: Boolean) = HourlyUi(
    id, getCorrect(isSelsiy, temp_c, temp_f), getUrl(icon_url), time
)
