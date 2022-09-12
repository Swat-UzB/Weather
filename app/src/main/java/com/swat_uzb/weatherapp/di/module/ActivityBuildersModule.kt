package com.swat_uzb.weatherapp.di.module

import com.swat_uzb.weatherapp.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    // here only activity uses
    @ContributesAndroidInjector(
        modules = [
            FragmentBuildersModule::class]
    )
    abstract fun contributeMainActivity(): MainActivity
}