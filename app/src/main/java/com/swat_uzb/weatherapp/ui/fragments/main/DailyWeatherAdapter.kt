package com.swat_uzb.weatherapp.ui.fragments.main

import android.annotation.SuppressLint
import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.data.model.mappers.TIME_FORMAT
import com.swat_uzb.weatherapp.databinding.ForecastItemViewBinding
import com.swat_uzb.weatherapp.domain.model.DailyUi
import com.swat_uzb.weatherapp.ui.fragments.add_location.BaseDiffUtil
import com.swat_uzb.weatherapp.utils.getDrawable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DailyWeatherAdapter @Inject constructor(private val application: Application) :
    ListAdapter<DailyUi, DailyWeatherAdapter.DailyWeatherViewHolder>(BaseDiffUtil<DailyUi>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyWeatherViewHolder {
        val binding = ForecastItemViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DailyWeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyWeatherViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    inner class DailyWeatherViewHolder(private val binding: ForecastItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(dailyForecastEntity: DailyUi) {
            with(binding) {
                forecastItemViewDayOfMonth.text =
                    application.getString(convertToDayOfWeek(dailyForecastEntity.day_of_week))
                forecastItemViewDayWeek.text = dailyForecastEntity.date
                forecastItemViewChanceOfRain.text = application.getString(
                    R.string.percentage_of,
                    dailyForecastEntity.chance_of_rain
                )
                forecastItemViewWeatherIcon
                    .setImageResource(dailyForecastEntity.icon_url.getDrawable())
                forecastItemViewMaxTemp.text =
                    application.getString(R.string.degree_sign, dailyForecastEntity.max_temp)
                forecastItemViewMinTemp.text =
                    application.getString(R.string.degree_sign, dailyForecastEntity.min_temp)
            }
        }

        @SuppressLint("SimpleDateFormat")
        private fun convertToDayOfWeek(date: String): Int {
            val currentDate = SimpleDateFormat(TIME_FORMAT).format(Date())
            if (date == currentDate) {
                return R.string.today_str
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
                0 -> R.string.str_saturday
                1 -> R.string.str_sunday
                2 -> R.string.str_monday
                3 -> R.string.str_tuesday
                4 -> R.string.str_wednesday
                5 -> R.string.str_thursday
                6 -> R.string.str_friday
                else -> R.string.something_wrong_str
            }
        }
    }
}
