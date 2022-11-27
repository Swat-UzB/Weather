package com.swat_uzb.weatherapp.ui.fragments.add_location

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

class BaseDiffUtil<T : Any> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(
        oldItem: T,
        newItem: T
    ): Boolean {
        return oldItem == newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: T,
        newItem: T
    ): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }
}