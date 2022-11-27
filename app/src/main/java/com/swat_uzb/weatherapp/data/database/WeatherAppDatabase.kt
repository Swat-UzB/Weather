package com.swat_uzb.weatherapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [CurrentWeatherEntity::class, DailyForecastEntity::class, HourlyDataEntity::class],
    version = 3
)
abstract class WeatherAppDatabase : RoomDatabase() {

    abstract fun currentWeatherDao(): CurrentWeatherDao

    abstract fun dailyForecastDao(): DailyForecastDao

    abstract fun hourlyDataDao(): HourlyDataDao

}
