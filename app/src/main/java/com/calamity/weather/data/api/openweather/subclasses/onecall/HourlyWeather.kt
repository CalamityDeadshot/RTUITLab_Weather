package com.calamity.weather.data.api.openweather.subclasses.onecall

import com.calamity.weather.data.api.openweather.subclasses.WeatherCondition
import com.google.gson.annotations.SerializedName

data class HourlyWeather(
    @SerializedName("dt") var currentTime: Int,
    @SerializedName("temp") var temperature: Float,
    @SerializedName("feels_like") var feelsLike: Float,
    @SerializedName("pressure") var pressure: Float,
    @SerializedName("humidity") var humidity: Float,
    @SerializedName("dew_point") var dewPoint: Double,
    @SerializedName("uvi") var uvIndex: Float,
    @SerializedName("clouds") var cloudiness: Int,
    @SerializedName("visibility") var visibility: Int,
    @SerializedName("wind_speed") var windSpeed: Float,
    @SerializedName("wind_deg") var windDirection: Float,
    @SerializedName("pop") var precipitationProbability: Float?,
    @SerializedName("weather") var weatherConditions: List<WeatherCondition>
)
