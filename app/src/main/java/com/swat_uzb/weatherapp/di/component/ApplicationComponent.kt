package com.swat_uzb.weatherapp.di.component

import android.app.Application
import com.swat_uzb.weatherapp.app.BaseApplication
import com.swat_uzb.weatherapp.di.module.ActivityBuildersModule
import com.swat_uzb.weatherapp.di.module.AppModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ActivityBuildersModule::class,
        AppModule::class]
)
interface ApplicationComponent : AndroidInjector<BaseApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): ApplicationComponent
    }
}