package com.swat_uzb.weatherapp.di.module

import android.app.Application
import androidx.room.Room
import com.swat_uzb.weatherapp.data.database.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideWeatherDb(application: Application): WeatherAppDatabase =
        Room.databaseBuilder(
            application.applicationContext,
            WeatherAppDatabase::class.java,
            "weather-db"
        )
            .addMigrations(MIGRATION_2_3)
            .build()

    @Singleton
    @Provides
    fun provideCurrentWeatherDao(weatherAppDatabase: WeatherAppDatabase): CurrentWeatherDao =
        weatherAppDatabase.currentWeatherDao()

    @Singleton
    @Provides
    fun provideDailyForecastDao(weatherAppDatabase: WeatherAppDatabase): DailyForecastDao =
        weatherAppDatabase.dailyForecastDao()


    @Singleton
    @Provides
    fun provideHourlyDataDao(weatherAppDatabase: WeatherAppDatabase): HourlyDataDao =
        weatherAppDatabase.hourlyDataDao()

}