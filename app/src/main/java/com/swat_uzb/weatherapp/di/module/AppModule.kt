package com.swat_uzb.weatherapp.di.module

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        AppBindModule::class,
        NetworkModule::class,
        DatabaseModule::class,
    ]
)
object AppModule {
// application dependencies instance that not change during life time of application
// like retrofit, room, glide, and so on

    @Provides
    @Singleton
    fun provideSharedPreferences(application: Application): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application)

//    @Provides
//    @Singleton
//    fun provideFirebaseAnalytic(application: Application): FirebaseAnalytics =
//        FirebaseAnalytics.getInstance(application)

    @Provides
    fun provideLocationServices(application: Application) =
        LocationServices.getFusedLocationProviderClient(application)

    @Provides
    fun provideConnectivityManager(application: Application) =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    fun provideLocationManager(application: Application) =
        application.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @Provides
    fun provideWorkManager(application: Application) = WorkManager.getInstance(application)

    @Provides
    fun provideNotificationManager(application: Application) = ContextCompat.getSystemService(
        application,
        NotificationManager::class.java
    ) as NotificationManager


}
