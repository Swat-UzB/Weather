package com.swat_uzb.weatherapp.domain

import com.swat_uzb.weatherapp.data.database.CurrentWeatherEntity
import com.swat_uzb.weatherapp.data.database.DailyForecastEntity
import com.swat_uzb.weatherapp.data.database.HourlyDataEntity
import com.swat_uzb.weatherapp.data.model.weatherapi.astronomy.AstronomyData
import com.swat_uzb.weatherapp.data.model.weatherapi.current.CurrentWeatherWeatherApiModel
import com.swat_uzb.weatherapp.data.model.weatherapi.forecast.ForecastData
import com.swat_uzb.weatherapp.data.model.weatherapi.search.Search
import com.swat_uzb.weatherapp.data.model.weatherapi.sport.Sport
import com.swat_uzb.weatherapp.domain.model.CurrentUi
import com.swat_uzb.weatherapp.domain.model.DailyUi
import com.swat_uzb.weatherapp.domain.model.HourlyUi
import com.swat_uzb.weatherapp.utils.Constants.DEFAULT_LANGUAGE
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    //  WeatherApiService

    suspend fun getCurrentFromWeatherApi(
        location: String, language: String = DEFAULT_LANGUAGE
    ): Result<CurrentWeatherWeatherApiModel>

    suspend fun getForecastFromWeatherApi(
        location: String, language: String = DEFAULT_LANGUAGE
    ): Result<ForecastData>

    suspend fun getSearchAutocompleteFromWeatherApi(
        location: String, language: String = DEFAULT_LANGUAGE
    ): Result<List<Search>>

    suspend fun getSportFromWeatherApi(
        location: String, language: String = DEFAULT_LANGUAGE
    ): Result<Sport>

    suspend fun getAstronomyFromWeatherApi(
        location: String, language: String = DEFAULT_LANGUAGE
    ): Result<AstronomyData>

    //  CurrentWeatherDao

    suspend fun insertCurrentData(currentWeatherEntity: CurrentWeatherEntity): Long

    suspend fun deleteCurrentData(currentId: Long)

    suspend fun loadCurrentWeather(currentId: Long): CurrentUi

    fun loadAllLocationsAsFlow(): Flow<List<CurrentUi>>

    suspend fun loadAllLocations(): List<CurrentUi>

    suspend fun checkCurrent(latitude: Double, longitude: Double): Boolean

    // DailyForecastDao

    suspend fun insertDailyForecast(forecast: ForecastData, currentId: Long)


    suspend fun updateDailyForecast(dailyForecastEntities: List<DailyForecastEntity>)


    suspend fun deleteDailyForecast(currentId: Long)


    suspend fun loadDailyForecast(currentId: Long): List<DailyUi>

    // HourlyDataEntity

    suspend fun insertHourlyData(forecastData: ForecastData, currentId: Long)


    suspend fun updateHourlyData(hourlyDataEntities: List<HourlyDataEntity>)


    suspend fun deleteHourlyData(currentId: Long)


    suspend fun loadHourlyData(currentId: Long): List<HourlyUi>

    suspend fun loadHourlyDataInHourlyDataEntity(currentId: Long): List<HourlyDataEntity>
}