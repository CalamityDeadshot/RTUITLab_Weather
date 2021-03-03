package com.calamity.weather.data.api.openweather.subclasses.onecall

import com.calamity.weather.data.api.openweather.subclasses.WeatherCondition
import com.google.gson.annotations.SerializedName

data class CurrentWeather(
    @SerializedName("dt") var currentTime: Long,
    @SerializedName("sunrise") var sunriseTime: Int,
    @SerializedName("sunset") var sunsetTime: Int,
    @SerializedName("temp") var temperature: Double,
    @SerializedName("feels_like") var feelsLike: Double,
    @SerializedName("dew_point") var dewPoint: Double,
    @SerializedName("clouds") var cloudiness: Int,
    @SerializedName("uvi") var uvIndex: Float,
    @SerializedName("visibility") var visibility: Int,
    @SerializedName("wind_speed") var windSpeed: Float,
    @SerializedName("wind_deg") var windDirection: Float,
    @SerializedName("weather") var weatherConditions: List<WeatherCondition>,
    @SerializedName("pop") var precipitationProbability: Float?,

    )
