package com.swat_uzb.weatherapp.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DailyForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyForecast(dailyForecastEntities: List<DailyForecastEntity>)

    @Update
    suspend fun updateDailyForecast(dailyForecastEntities: List<DailyForecastEntity>)

    @Query("DELETE FROM daily_data WHERE currentId=:currentId")
    suspend fun deleteDailyData(currentId: Long)

    @Query("SELECT * FROM daily_data WHERE currentId =:currentId")
     suspend fun loadDailyForecast(currentId: Long): List<DailyForecastEntity>
}