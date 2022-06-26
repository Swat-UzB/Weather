package com.swat_uzb.weatherapp.data.network


import com.swat_uzb.weatherapp.data.model.weatherapi.astronomy.AstronomyData
import com.swat_uzb.weatherapp.data.model.weatherapi.current.CurrentWeatherWeatherApiModel
import com.swat_uzb.weatherapp.data.model.weatherapi.forecast.ForecastData
import com.swat_uzb.weatherapp.data.model.weatherapi.search.Search
import com.swat_uzb.weatherapp.data.model.weatherapi.sport.Sport
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherApiService {

    // WeatherAPI
    @GET("current.json")
    suspend fun getCurrentFromWeatherApi(
        @Query("q") location: String,
        @Query("aqi") aqi: String = "yes",
        @Query("lang") language: String = "en"
    ): CurrentWeatherWeatherApiModel


    @GET("forecast.json")
    suspend fun getForecastFromWeatherApi(
        @Query("q") location: String,
        @Query("days") days: Int = 10,
        @Query("lang") language: String = "en"
//        @Query("alerts") alerts: String = "yes"
    ): ForecastData

    @GET("search.json")
    suspend fun getSearchAutocompleteFromWeatherApi(
        @Query("q") location: String,
        @Query("lang") language: String = "en"
    ): List<Search>

    @GET("sports.json")
    suspend fun getSportFromWeatherApi(
        @Query("q") location: String,
        @Query("lang") language: String = "en"
    ): Sport


    @GET("astronomy.json")
    suspend fun getAstronomyFromWeatherApi(
        @Query("q") location: String,
        @Query("lang") language: String = "en"
    ): AstronomyData
}