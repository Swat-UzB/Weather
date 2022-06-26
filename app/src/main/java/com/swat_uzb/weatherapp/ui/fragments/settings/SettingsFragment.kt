package com.swat_uzb.weatherapp.ui.fragments.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.swat_uzb.weatherapp.R


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(
            R.xml.preference_settings,
            rootKey
        )
    }
}