package com.calamity.weather.data.api.openweather

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calamity.weather.data.api.openweather.subclasses.onecall.CurrentWeather
import com.calamity.weather.data.api.openweather.subclasses.onecall.DailyWeather
import com.calamity.weather.data.api.openweather.subclasses.onecall.HourlyWeather
import com.google.gson.annotations.SerializedName
@Entity(tableName = "weather")
data class Weather(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @SerializedName("lat") var latitude: Double,
    @SerializedName("lon") var longitude: Double,
    @SerializedName("timezone") var timezone: String,
    @SerializedName("timezone_offset") var timezoneOffset: Int,
    @SerializedName("current") var weather: CurrentWeather,
    @SerializedName("hourly") var hourly: List<HourlyWeather>,
    @SerializedName("daily") var daily: List<DailyWeather>,
    //@SerializedName("minutely") var minutely: List<CurrentWeather>?,
    var isLocationEntry: Boolean,
    // Obtained from Places API, used to display location name to provide
    // consistency because Places name and openweather name differ
    var cityName: String = "",
    var cityId: Int = 0,

    // Places API place ID
    var placeId: String? = null
) {

}