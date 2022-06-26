package com.swat_uzb.weatherapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(
    entities = [CurrentWeatherEntity::class, DailyForecastEntity::class, HourlyDataEntity::class],
    version = 2
)
abstract class WeatherAppDatabase : RoomDatabase() {

    abstract fun currentWeatherDao(): CurrentWeatherDao

    abstract fun dailyForecastDao(): DailyForecastDao

    abstract fun hourlyDataDao(): HourlyDataDao

}