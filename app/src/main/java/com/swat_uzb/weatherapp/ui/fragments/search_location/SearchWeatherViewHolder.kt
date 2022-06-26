package com.swat_uzb.weatherapp.ui.fragments.search_location

import androidx.recyclerview.widget.RecyclerView
import com.swat_uzb.weatherapp.data.model.weatherapi.search.Search
import com.swat_uzb.weatherapp.databinding.SearchLocationItemViewBinding


class SearchWeatherViewHolder(private val binding: SearchLocationItemViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bindData(search: Search) {
        binding.search = search
    }
}