package com.swat_uzb.weatherapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "hourly_data",
    foreignKeys = [ForeignKey(
        entity = CurrentWeatherEntity::class,
        parentColumns = ["current_id"],
        childColumns = ["currentId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class HourlyDataEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "hourly_data_id")
    val id: Long = 0,
    val chance_of_rain: Int,
    val chance_of_snow: Int,
    val cloud: Int,
    val condition: String,
    val icon_url: String,
    val feels_like_c: Int,
    val feels_like_f: Int,
    val humidity: Int,
    val is_day: Int,
    val pressure_in: Double,
    val pressure_mb: Double,
    val temp_c: Int,
    val temp_f: Int,
    val time: String,
    val time_epoch: Int,
    val uv: Double,
    val vis_km: Double,
    val vis_miles: Double,
    val will_it_rain: Int,
    val will_it_snow: Int,
    val wind_degree: Int,
    val wind_dir: String,
    val wind_kph: Int,
    val wind_mph: Int,
    @ColumnInfo(index = true) val currentId: Long
)
