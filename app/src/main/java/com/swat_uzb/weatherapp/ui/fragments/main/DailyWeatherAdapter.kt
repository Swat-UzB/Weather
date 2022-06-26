package com.swat_uzb.weatherapp.ui.fragments.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.swat_uzb.weatherapp.databinding.ForecastItemViewBinding
import com.swat_uzb.weatherapp.domain.model.DailyUi
import com.swat_uzb.weatherapp.ui.fragments.add_location.BaseDiffUtil
import javax.inject.Inject

class DailyWeatherAdapter @Inject constructor() :
    ListAdapter<DailyUi, DailyWeatherViewHolder>(BaseDiffUtil<DailyUi>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyWeatherViewHolder {
        val binding = ForecastItemViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DailyWeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyWeatherViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }
}