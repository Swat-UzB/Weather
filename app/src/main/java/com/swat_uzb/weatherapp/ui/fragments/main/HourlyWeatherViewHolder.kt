package com.swat_uzb.weatherapp.ui.fragments.main

import androidx.recyclerview.widget.RecyclerView
import com.swat_uzb.weatherapp.databinding.HourlyWeatherItemViewBinding
import com.swat_uzb.weatherapp.domain.model.HourlyUi

class HourlyWeatherViewHolder(private val binding: HourlyWeatherItemViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bindData(hourlyUi: HourlyUi) {
        binding.hourlyData = hourlyUi
    }

}