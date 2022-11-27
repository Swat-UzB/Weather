package com.swat_uzb.weatherapp.ui.fragments.search_location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swat_uzb.weatherapp.data.model.weatherapi.search.Search
import com.swat_uzb.weatherapp.databinding.SearchLocationItemViewBinding
import com.swat_uzb.weatherapp.ui.fragments.add_location.BaseDiffUtil
import javax.inject.Inject

class SearchAdapter @Inject constructor() :
    ListAdapter<Search, SearchAdapter.SearchWeatherViewHolder>(BaseDiffUtil<Search>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchWeatherViewHolder {
        val binding =
            SearchLocationItemViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return SearchWeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchWeatherViewHolder, position: Int) {
        val search = getItem(position)
        holder.bindData(search)
        holder.itemView.setOnClickListener {
            onItemClickListener?.let { click ->
                click(search)
            }
        }

    }

    private var onItemClickListener: ((Search) -> Unit)? = null

    fun setOnItemClickListener(listener: (Search) -> Unit) {
        onItemClickListener = listener
    }

    inner class SearchWeatherViewHolder(private val binding: SearchLocationItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(search: Search) {
            with(binding) {
                searchLocationItemViewRegionTextView.text = search.name
                searchLocationItemViewCountryTextView.text =
                    bindCountryName(search.region, search.country)
            }
        }

        private fun bindCountryName(region: String?, country: String?) = when {
            region.isNullOrBlank() -> country
            country.isNullOrBlank() -> ""
            else -> "$region, $country"
        }


    }

}