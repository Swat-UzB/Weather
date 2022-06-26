package com.swat_uzb.weatherapp.di.module

import com.swat_uzb.weatherapp.data.repository.WeatherRepositoryImpl
import com.swat_uzb.weatherapp.domain.WeatherRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class RepoModule {

    @Singleton
    @Binds
    abstract fun bindWeatherRepository(weatherRepository: WeatherRepositoryImpl): WeatherRepository
}