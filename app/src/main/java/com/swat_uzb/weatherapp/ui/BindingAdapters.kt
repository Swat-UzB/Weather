package com.swat_uzb.weatherapp.ui


import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.swat_uzb.weatherapp.utils.Constants.PACKAGE_NAME
import java.lang.Exception


        @BindingAdapter("region", "country")
        fun bindCountryName(view: TextView, region: String, country: String) {
            view.text = if (region.isEmpty()) {
                country
            } else {
                "$region, $country"
            }
        }

        @BindingAdapter("android:load_image")
        fun bindLoadData(view: ImageView, url: String?) {
            url?.let {
                try {
                val imageResource = view.resources.getIdentifier(url, null, PACKAGE_NAME)
                val drawable = ContextCompat.getDrawable(view.context, imageResource)
                view.setImageDrawable(drawable)

                }catch (ex :Exception){

                }

            }
        }

        @BindingAdapter("android:uv")
        fun bindUvIndex(view: TextView, index: Int) {
            view.text = when (index) {
                in 0..2 -> "Low"
                in 3..5 -> "Moderate"
                6, 7 -> "High"
                in 8..10 -> "Very High"
                else -> "Extreme"
            }
        }