package com.calamity.weather.data.api.openweather.subclasses.onecall

import com.calamity.weather.data.api.openweather.subclasses.WeatherCondition
import com.google.gson.annotations.SerializedName

data class DailyWeather(
    @SerializedName("dt") var currentTime: Long,
    @SerializedName("sunrise") var sunriseTime: Int,
    @SerializedName("sunset") var sunsetTime: Int,
    @SerializedName("temp") var temperature: Temperature,
    @SerializedName("feels_like") var feelsLike: Temperature,
    @SerializedName("pressure") var pressure: Float,
    @SerializedName("humidity") var humidity: Float,
    @SerializedName("dew_point") var dewPoint: Double,
    @SerializedName("wind_speed") var windSpeed: Float,
    @SerializedName("wind_deg") var windDirection: Float,
    @SerializedName("clouds") var cloudiness: Int,
    @SerializedName("uvi") var uvIndex: Float,
    @SerializedName("weather") var weatherConditions: List<WeatherCondition>
)
