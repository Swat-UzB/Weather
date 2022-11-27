package com.swat_uzb.weatherapp.data.repository

import com.swat_uzb.weatherapp.data.database.*
import com.swat_uzb.weatherapp.data.model.mappers.*
import com.swat_uzb.weatherapp.data.model.weatherapi.astronomy.AstronomyData
import com.swat_uzb.weatherapp.data.model.weatherapi.current.CurrentWeatherWeatherApiModel
import com.swat_uzb.weatherapp.data.model.weatherapi.forecast.ForecastData
import com.swat_uzb.weatherapp.data.model.weatherapi.search.Search
import com.swat_uzb.weatherapp.data.model.weatherapi.sport.Sport
import com.swat_uzb.weatherapp.data.network.WeatherApiService
import com.swat_uzb.weatherapp.domain.WeatherRepository
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import com.swat_uzb.weatherapp.domain.model.DailyUi
import com.swat_uzb.weatherapp.domain.model.HourlyUi
import com.swat_uzb.weatherapp.utils.SharedPreferencesHelper
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val currentWeatherDao: CurrentWeatherDao,
    private val dailyForecastDao: DailyForecastDao,
    private val hourlyDataDao: HourlyDataDao,
    private val prefs: SharedPreferencesHelper
) : WeatherRepository {

    override suspend fun getCurrentFromWeatherApi(
        location: String,
        language: String
    ): Result<CurrentWeatherWeatherApiModel> = kotlin.runCatching {
        weatherApiService.getCurrentFromWeatherApi(
            location = location,
            language = language
        )
    }

    override suspend fun getForecastFromWeatherApi(
        location: String,
        language: String
    ): Result<ForecastData> = kotlin.runCatching {
        weatherApiService.getForecastFromWeatherApi(
            location = location,
            language = language
        )
    }

    override suspend fun getSearchAutocompleteFromWeatherApi(
        location: String,
        language: String
    ): Result<List<Search>> = kotlin.runCatching {
        weatherApiService.getSearchAutocompleteFromWeatherApi(
            location = location,
            language = language
        )
    }

    override suspend fun getSportFromWeatherApi(
        location: String,
        language: String
    ): Result<Sport> = kotlin.runCatching {
        weatherApiService.getSportFromWeatherApi(
            location = location, language = language
        )
    }

    override suspend fun getAstronomyFromWeatherApi(
        location: String,
        language: String
    ): Result<AstronomyData> = kotlin.runCatching {
        weatherApiService.getAstronomyFromWeatherApi(
            location = location, language = language
        )
    }

    override suspend fun insertCurrentData(
        currentWeatherEntity: CurrentWeatherEntity
    ): Long {
        return currentWeatherDao.insertCurrentData(currentWeatherEntity)
    }

    override suspend fun deleteCurrentData(currentId: Long) {
        currentWeatherDao.deleteCurrentDataById(currentId)
    }

    override suspend fun loadCurrentWeather(currentId: Long): CurrentUi {
        return currentWeatherDao.loadCurrentWeather(currentId).toCurrentUi(
            prefs.getTemperature(),
            prefs.getWindSpeed()
        )

    }

    override fun loadAllLocationsAsFlow() = currentWeatherDao.loadAllLocationsAsFlow().map { list ->
        list.map {
            it.toCurrentUi(
                prefs.getTemperature(),
                prefs.getWindSpeed()
            )
        }
    }

    override suspend fun loadAllLocations() = currentWeatherDao.loadAllLocations().map { list ->
        list.toCurrentUi(
            prefs.getTemperature(),
            prefs.getWindSpeed()
        )
    }

    override suspend fun checkCurrent(latitude: Double, longitude: Double): Boolean {
        return currentWeatherDao.isExists(latitude, longitude)
    }

    override suspend fun insertDailyForecast(forecast: ForecastData, currentId: Long) {
        dailyForecastDao.insertDailyForecast(
            forecast.forecast.forecastDay.map { daily ->
                toDailyForecastEntity(daily.day, currentId, daily.date)
            })
    }

    override suspend fun updateDailyForecast(dailyForecastEntities: List<DailyForecastEntity>) {
        dailyForecastDao.updateDailyForecast(dailyForecastEntities)
    }

    override suspend fun deleteDailyForecast(currentId: Long) {
        dailyForecastDao.deleteDailyData(currentId)
    }

    override suspend fun loadDailyForecast(currentId: Long): List<DailyUi> {
        return dailyForecastDao.loadDailyForecast(currentId).map {
            it.toDailyUi(prefs.getTemperature())
        }
    }

    override suspend fun insertHourlyData(forecastData: ForecastData, currentId: Long) {
        forecastData.forecast.forecastDay.map { forecasts ->
            hourlyDataDao.insertHourlyData(
                forecasts.hour.map { hour ->
                    toHourlyDataEntity(hour, currentId)
                })
        }
    }

    override suspend fun updateHourlyData(hourlyDataEntities: List<HourlyDataEntity>) {
        hourlyDataDao.updateHourlyData(hourlyDataEntities)
    }

    override suspend fun deleteHourlyData(currentId: Long) {
        hourlyDataDao.deleteHourlyData(currentId)
    }

    override suspend fun loadHourlyData(currentId: Long): List<HourlyUi> {
        return hourlyDataDao.loadHourlyData(currentId).map {
            it.toHourlyUi(prefs.getTemperature())
        }
    }

    override suspend fun loadHourlyDataInHourlyDataEntity(currentId: Long): List<HourlyDataEntity> {
        return hourlyDataDao.loadHourlyData(currentId)
    }
}