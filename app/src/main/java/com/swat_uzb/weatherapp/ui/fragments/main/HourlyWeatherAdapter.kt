package com.swat_uzb.weatherapp.ui.fragments.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swat_uzb.weatherapp.databinding.HourlyWeatherItemViewBinding
import com.swat_uzb.weatherapp.domain.model.HourlyUi
import com.swat_uzb.weatherapp.ui.fragments.add_location.BaseDiffUtil
import javax.inject.Inject

class HourlyWeatherAdapter @Inject constructor() :
    ListAdapter<HourlyUi, HourlyWeatherViewHolder>(BaseDiffUtil<HourlyUi>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyWeatherViewHolder {
        val binding =
            HourlyWeatherItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HourlyWeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyWeatherViewHolder, position: Int) {
        val hourlyUi = getItem(position)
        holder.bindData(hourlyUi)
    }
}