package com.swat_uzb.weatherapp.ui.fragments.add_location

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swat_uzb.weatherapp.R
import com.swat_uzb.weatherapp.databinding.LocationsItemViewBinding
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import com.swat_uzb.weatherapp.utils.getDrawable
import javax.inject.Inject

class LocationsAdapter @Inject constructor(private val application: Application) :
    ListAdapter<CurrentUi, LocationsAdapter.LocationsViewHolder>(BaseDiffUtil<CurrentUi>()) {

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


    inner class LocationsViewHolder(private val binding: LocationsItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var current = false

        fun bindData(currentWeather: CurrentUi) {
            with(binding) {
                current = currentWeather.current_location
                locationsItemViewRegion.text = currentWeather.location
                locationsItemViewCurrentTemp.text =
                    application.getString(R.string.degree_sign, currentWeather.temp)
                locationsItemViewFeelsLikeTemp.text =
                    application.getString(R.string.feels_like, currentWeather.feels_like)
                locationsItemViewChanceOfRain.text =
                    application.getString(
                        R.string.percentage_of,
                        currentWeather.chance_of_rain
                    )
                locationsItemViewCurrentWeatherIcon
                    .setImageResource(currentWeather.icon_url.getDrawable())


            }
        }
    }

}