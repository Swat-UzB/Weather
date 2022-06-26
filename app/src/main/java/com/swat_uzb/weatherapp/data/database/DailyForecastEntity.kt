package com.swat_uzb.weatherapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_data",
    foreignKeys = [androidx.room.ForeignKey(
        entity = CurrentWeatherEntity::class,
        parentColumns = ["current_id"],
        childColumns = ["currentId"],
        onDelete = androidx.room.ForeignKey.CASCADE
    )]
)
data class DailyForecastEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "daily_forecast_id")
    val id: Long = 0,
    val chance_of_rain: Double,
    val condition: String,
    val date: String,
    val icon_url: String,
    val max_temp_c: Int,
    val max_temp_f: Int,
    val min_temp_c: Int,
    val min_temp_f: Int,
    @ColumnInfo(index = true) val currentId: Long
)
