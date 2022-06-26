package com.swat_uzb.weatherapp.di.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.net.ConnectivityManager
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import com.swat_uzb.weatherapp.BuildConfig
import com.swat_uzb.weatherapp.data.database.CurrentWeatherDao
import com.swat_uzb.weatherapp.data.database.DailyForecastDao
import com.swat_uzb.weatherapp.data.database.HourlyDataDao
import com.swat_uzb.weatherapp.data.database.WeatherAppDatabase
import com.swat_uzb.weatherapp.data.network.WeatherApiService
import com.swat_uzb.weatherapp.utils.Constants.BASE_URL_WEATHER_API
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
object AppModule {
// application dependencies instance that not change during life time of application
// like retrofit, room, glide, and so on

    @Singleton
    @Provides
    fun provideWeatherService(client: OkHttpClient): WeatherApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_WEATHER_API)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().apply {
        addInterceptor { chain ->
            val url = chain.request().url
                .newBuilder()
                .addQueryParameter("key", BuildConfig.APP_ID_WEATHER_API)
                .build()
            chain.proceed(chain.request().newBuilder().url(url).build())
        }
    }.build()


    @Singleton
    @Provides
    fun provideWeatherDb(application: Application): WeatherAppDatabase =
        Room.databaseBuilder(
            application.applicationContext,
            WeatherAppDatabase::class.java,
            "weather-db"
        )
            .fallbackToDestructiveMigration()
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

    @Provides
    @Singleton
    fun provideSharedPreferences(application: Application): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application)

    @Provides
    @Singleton
    fun provideFirebaseAnalytic(application: Application): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(application)

    @Provides
    @Singleton
    fun provideLocationServices(application: Application) =
        LocationServices.getFusedLocationProviderClient(application)

    @Provides
    @Singleton
    fun provideConnectivityManager(application: Application) =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    @Singleton
    fun provideLocationManager(application: Application) =
        application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
}
