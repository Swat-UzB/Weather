package com.swat_uzb.weatherapp.data.database

import androidx.room.*

@Dao
interface HourlyDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyData(hourlyDataEntities: List<HourlyDataEntity>)

    @Update
    suspend fun updateHourlyData(hourlyDataEntities: List<HourlyDataEntity>)

    @Query("DELETE FROM hourly_data WHERE currentId=:currentId")
    suspend fun deleteHourlyData(currentId: Long)

    @Query("SELECT * FROM hourly_data WHERE currentId =:currentId")
    suspend fun loadHourlyData(currentId: Long): List<HourlyDataEntity>
}