package com.calamity.weather.data.api.openweather

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calamity.weather.data.api.openweather.subclasses.*
import com.google.gson.annotations.SerializedName

/*
    Actual API response:
    not to be confused with com.calamity.weather.data.api.sublcasses.CurrentWeather.kt,
    which is a subclass for com.calamity.weather.data.api.openweather.Weather.kt
*/
@Entity(tableName = "current_weather")
data class CurrentWeather(

    @PrimaryKey(autoGenerate = true) var db_id: Int = 0,

    @SerializedName("coord") var coordinates: Coord,
    @SerializedName("weather") var weatherConditions: List<WeatherCondition>,
    @SerializedName("main") var main: WeatherMain,
    @SerializedName("visibility") var visibility: Int,
    @SerializedName("wind") var wind: Wind,
    @SerializedName("clouds") var clouds: Clouds,
    @SerializedName("dt") var calculationTime: Int,
    @SerializedName("sys") var weatherSystem: WeatherSystem,
    @SerializedName("timezone") var timezone: Int,
    @SerializedName("id") var cityId: Int,
    @SerializedName("name") var cityName: String,
    var isLocationEntry: Boolean
) {
}