package com.swat_uzb.weatherapp.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.swat_uzb.weatherapp.R

fun hideKeyboard(activity: Activity) {
    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocusedView = activity.currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}

fun String.getDrawable() = when (this) {
    "day/113" -> R.drawable._13_day
    "night/113" -> R.drawable._13_night
    "day/116" -> R.drawable._16_day
    "night/116" -> R.drawable._16_night
    "day/119" -> R.drawable._19_day
    "night/119" -> R.drawable._19_night
    "day/122", "night/122" -> R.drawable._22_day
    "day/143", "night/143" -> R.drawable._43_day
    "day/176", "day/293", "day/299", "day/353" -> R.drawable._76_day
    "night/176", "night/293", "night/299", "night/353" -> R.drawable._76_night
    "day/179", "day/323", "day/329", "day/368" -> R.drawable._79_day
    "night/179", "night/323", "night/329", "night/335", "night/368" -> R.drawable._79_night
    "day/182", "day/362" -> R.drawable._82_day
    "night/182", "night/362" -> R.drawable._82_night
    "day/185", "day/281", "night/185", "night/281" -> R.drawable._85_day
    "day/200", "day/386" -> R.drawable._00_day
    "night/200", "night/386" -> R.drawable._00_night
    "day/227", "night/227" -> R.drawable._27_day
    "day/230", "night/230" -> R.drawable._30_day
    "day/248", "night/248" -> R.drawable._48_day
    "day/260", "night/260" -> R.drawable._60_day
    "day/263", "day/266", "night/263", "night/266" -> R.drawable._63_day
    "day/284", "night/284" -> R.drawable._84_day
    "day/296", "day/302", "night/296", "night/302" -> R.drawable._96_day
    "day/305", "day/356" -> R.drawable._05_day
    "night/305", "night/356" -> R.drawable._05_night
    "day/308", "night/308" -> R.drawable._08_day
    "day/311", "day/314", "night/311", "night/314" -> R.drawable._11_day
    "day/317", "day/320", "night/317", "night/320" -> R.drawable._17_day
    "day/326", "day/332", "night/326", "night/332" -> R.drawable._26_day
    "day/335", "day/371" -> R.drawable._35_day
    "day/338", "night/338" -> R.drawable._38_day
    "day/350", "night/350" -> R.drawable._50_day
    "day/359" -> R.drawable._59_day
    "night/359" -> R.drawable._59_night
    "day/365" -> R.drawable._65_day
    "night/365" -> R.drawable._65_night
    "night/371" -> R.drawable._71_night
    "day/374" -> R.drawable._74_day
    "night/374" -> R.drawable._74_night
    "day/377" -> R.drawable._77_day
    "night/377" -> R.drawable._77_night
    "day/389", "night/389" -> R.drawable._89_day
    "day/392" -> R.drawable._92_day
    "night/392" -> R.drawable._92_night
    "day/395", "night/395" -> R.drawable._95_day
    else -> {
        R.drawable._13_day
    }
}
