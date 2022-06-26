package com.swat_uzb.weatherapp.app

import com.swat_uzb.weatherapp.di.component.DaggerApplicationComponent
import dagger.android.DaggerApplication

class BaseApplication : DaggerApplication() {
    override fun applicationInjector() =
        DaggerApplicationComponent.builder().application(this).build()


}