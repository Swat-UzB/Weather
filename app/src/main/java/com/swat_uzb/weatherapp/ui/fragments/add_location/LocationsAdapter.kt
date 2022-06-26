package com.swat_uzb.weatherapp.ui.fragments.add_location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.swat_uzb.weatherapp.databinding.LocationsItemViewBinding
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import javax.inject.Inject

class LocationsAdapter @Inject constructor() :
    ListAdapter<CurrentUi, LocationsViewHolder>(BaseDiffUtil<CurrentUi>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationsViewHolder {
        val binding =
            LocationsItemViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return LocationsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationsViewHolder, position: Int) {
        val currentWeather = getItem(position)
        holder.bindData(currentWeather)
        holder.itemView.setOnClickListener {
            onItemClickListener?.let { click ->
                click(currentWeather)
            }
        }

    }

    private var onItemClickListener: ((CurrentUi) -> Unit)? = null

    fun setOnItemClickListener(listener: (CurrentUi) -> Unit) {
        onItemClickListener = listener
    }

}