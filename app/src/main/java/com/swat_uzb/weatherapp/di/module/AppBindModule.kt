package com.swat_uzb.weatherapp.di.module

import com.swat_uzb.weatherapp.data.repository.WeatherRepositoryImpl
import com.swat_uzb.weatherapp.domain.WeatherRepository
import com.swat_uzb.weatherapp.utils.ConnectivityObserver
import com.swat_uzb.weatherapp.utils.NetworkConnectivityObserver
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface AppBindModule {

    @Singleton
    @Binds
    fun bindWeatherRepository(weatherRepository: WeatherRepositoryImpl): WeatherRepository

    @Singleton
    @Binds
    fun bindNetworkConnectivityObserver(networkConnectivityObserver: NetworkConnectivityObserver): ConnectivityObserver
}