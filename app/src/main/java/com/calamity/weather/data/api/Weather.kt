package com.calamity.weather.data.api

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calamity.weather.data.api.subclasses.CurrentWeather
import com.google.gson.annotations.SerializedName
@Entity(tableName = "weather")
class Weather(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @SerializedName("lat") var latitude: Double,
    @SerializedName("lon") var longitude: Double,
    @SerializedName("timezone") var timezone: Int,
    @SerializedName("timezone_offset") var timezoneOffset: Int,
    @SerializedName("current") var weather: CurrentWeather,
) {
}