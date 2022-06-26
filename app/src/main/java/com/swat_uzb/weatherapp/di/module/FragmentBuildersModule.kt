package com.swat_uzb.weatherapp.di.module

import com.swat_uzb.weatherapp.ui.fragments.add_location.AddLocationFragment
import com.swat_uzb.weatherapp.ui.fragments.main.MainFragment
import com.swat_uzb.weatherapp.ui.fragments.search_location.SearchLocationFragment
import com.swat_uzb.weatherapp.ui.fragments.settings.SettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeAddLocationFragment(): AddLocationFragment

    @ContributesAndroidInjector()
    abstract fun contributeSearchLocationFragment(): SearchLocationFragment
}