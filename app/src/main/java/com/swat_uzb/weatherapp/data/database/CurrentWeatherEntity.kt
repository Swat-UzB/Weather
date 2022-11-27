package com.swat_uzb.weatherapp.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Entity(tableName = "current_data")
data class CurrentWeatherEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "current_id")
    val id: Long,
    val condition: String,
    val country: String,
    val daytime: Boolean,
    val feels_like_c: Int,
    val feels_like_f: Int,
    val humidity: Int,
    val icon_url: String,
    val latitude: Double,
    val local_time: String,
    val location: String,
    val longitude: Double,
    val region: String,
    val temp_c: Int,
    val temp_f: Int,
    val uv: Double,
    val wind_degree: Int,
    val wind_direction: String,
    val wind_kph: Int,
    val wind_mph: Int,
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    val chance_of_rain:Int,
    val current_location: Boolean,
    val place_id: Long = id

)

val MIGRATION_2_3 = object :Migration(2,3){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE current_data ADD COLUMN chance_of_rain INTEGER NOT NULL DEFAULT 0")
    }

}
