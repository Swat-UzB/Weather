package com.swat_uzb.weatherapp.ui.fragments.add_location

import androidx.recyclerview.widget.RecyclerView
import com.swat_uzb.weatherapp.databinding.LocationsItemViewBinding
import com.swat_uzb.weatherapp.domain.model.CurrentUi

class LocationsViewHolder(private val binding: LocationsItemViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    var current = false

    fun bindData(currentWeather: CurrentUi) {
        binding.currentWeather = currentWeather
        current = currentWeather.current_location
    }
}