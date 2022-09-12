package com.swat_uzb.weatherapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentWeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentData(currentWeatherEntity: CurrentWeatherEntity): Long

    @Query("DELETE  FROM current_data WHERE  current_id=:currentId")
    suspend fun deleteCurrentDataById(currentId: Long)

    @Query("SELECT * FROM current_data WHERE current_id=:currentId")
   suspend fun loadCurrentWeather(currentId: Long): CurrentWeatherEntity

    @Query("SELECT * FROM current_data ")
    fun loadAllLocationsAsFlow(): Flow<List<CurrentWeatherEntity>>

    @Query("SELECT * FROM current_data ORDER BY current_location DESC")
   suspend fun loadAllLocations(): List<CurrentWeatherEntity>

    @Query("SELECT EXISTS(SELECT * FROM current_data WHERE latitude=:latitude AND longitude =:longitude) ")
    suspend fun isExists(latitude: Double, longitude: Double): Boolean

}