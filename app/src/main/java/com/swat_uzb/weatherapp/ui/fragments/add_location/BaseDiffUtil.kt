package com.swat_uzb.weatherapp.ui.fragments.add_location

import androidx.recyclerview.widget.DiffUtil
import com.swat_uzb.weatherapp.domain.model.CurrentUi

class BaseDiffUtil<T> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(
        oldItem: T,
        newItem: T
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: T,
        newItem: T
    ): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }
}