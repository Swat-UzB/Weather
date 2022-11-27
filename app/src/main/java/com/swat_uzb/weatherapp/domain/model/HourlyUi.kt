package com.swat_uzb.weatherapp.domain.model

import android.annotation.SuppressLint
import com.swat_uzb.weatherapp.utils.Constants
import java.text.SimpleDateFormat
import java.util.*


data class HourlyUi(
    val id: Long,
    val temp: Int,
    val icon_url: String,
    val date: String

)

@SuppressLint("SimpleDateFormat")
fun HourlyUi.compareDate(): Boolean {
    val simpleDate = SimpleDateFormat(Constants.TIME_STRING_FORMAT)
    val hourlyDate = simpleDate.parse(date)
    val calendar = Calendar.getInstance()
    val today = calendar.time
    calendar.add(Calendar.DAY_OF_WEEK, 1)
    val tomorrow = calendar.time
    return today.before(hourlyDate) && tomorrow.after(hourlyDate)
}
