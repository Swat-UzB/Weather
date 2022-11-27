package com.swat_uzb.weatherapp.app

import androidx.work.Configuration
import androidx.work.WorkManager
import com.swat_uzb.weatherapp.di.component.DaggerApplicationComponent
import com.swat_uzb.weatherapp.workmanager.SampleWorkerFactory
import dagger.android.DaggerApplication
import javax.inject.Inject

class BaseApplication : DaggerApplication() {

    @Inject
    lateinit var workerFactory: SampleWorkerFactory

    override fun applicationInjector() =
        DaggerApplicationComponent.builder().application(this).build()

    override fun onCreate() {
        super.onCreate()

        WorkManager.initialize(
            this,
            Configuration.Builder().setWorkerFactory(workerFactory).build()
        )
    }

}