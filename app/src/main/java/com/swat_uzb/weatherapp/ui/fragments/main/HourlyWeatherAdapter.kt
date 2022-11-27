package com.swat_uzb.weatherapp.ui.fragments.main

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.databinding.HourlyWeatherItemViewBinding
import com.swat_uzb.weatherapp.domain.model.HourlyUi
import com.swat_uzb.weatherapp.ui.fragments.add_location.BaseDiffUtil
import com.swat_uzb.weatherapp.utils.getDrawable
import javax.inject.Inject

class HourlyWeatherAdapter @Inject constructor(private val application: Application) :
    ListAdapter<HourlyUi, HourlyWeatherAdapter.HourlyWeatherViewHolder>(BaseDiffUtil<HourlyUi>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyWeatherViewHolder {
        val binding =
            HourlyWeatherItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HourlyWeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyWeatherViewHolder, position: Int) {
        val hourlyUi = getItem(position)
        holder.bindData(hourlyUi)
    }

    inner class HourlyWeatherViewHolder(private val binding: HourlyWeatherItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(hourlyUi: HourlyUi) {
            with(binding) {
                fragmentMainHourlyWeatherTime.text =
                    hourlyUi.date.substring(hourlyUi.date.length - 5)
                fragmentMainHourlyWeatherIcon
                    .setImageResource(hourlyUi.icon_url.getDrawable())
                fragmentMainHourlyWeatherTemp.text =
                    application.getString(R.string.degree_sign, hourlyUi.temp)
            }

        }
    }
}