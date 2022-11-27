package com.swat_uzb.weatherapp.di.module

import com.swat_uzb.weatherapp.BuildConfig
import com.swat_uzb.weatherapp.data.network.WeatherApiService
import com.swat_uzb.weatherapp.utils.Constants
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {
    @Singleton
    @Provides
    fun provideWeatherService(): WeatherApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_WEATHER_API)
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }


    private fun provideOkHttpClient(): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader(Constants.KEY_VALUE_TOKEN, BuildConfig.APP_ID_WEATHER_API)
                .build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder().addInterceptor(authInterceptor)
            .connectTimeout(OkHttp_Time_Out,TimeUnit.SECONDS)
            .build()

    }
    companion object{
        private const val BASE_URL_WEATHER_API = "https://api.weatherapi.com/v1/"
        private const val OkHttp_Time_Out = 5L
    }
}