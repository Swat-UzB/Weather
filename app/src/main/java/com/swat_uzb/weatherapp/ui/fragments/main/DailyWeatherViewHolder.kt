package com.swat_uzb.weatherapp.ui.fragments.main

import androidx.recyclerview.widget.RecyclerView
import com.swat_uzb.weatherapp.databinding.ForecastItemViewBinding
import com.swat_uzb.weatherapp.domain.model.DailyUi

class DailyWeatherViewHolder(private val binding: ForecastItemViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bindData(dailyForecastEntity: DailyUi) {
        binding.dailyForecast = dailyForecastEntity
    }

}