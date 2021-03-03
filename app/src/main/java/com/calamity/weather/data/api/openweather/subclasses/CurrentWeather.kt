package com.calamity.weather.data.api.openweather.subclasses

import com.google.gson.annotations.SerializedName

data class CurrentWeather(
    @SerializedName("dt") var currentTime: Int,
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
    @SerializedName("pop") var precipitationProbability: Int?,
    @SerializedName("hourly") var hourly: List<CurrentWeather>?,
    @SerializedName("daily") var daily: List<CurrentWeather>?,
    @SerializedName("minutely") var minutely: List<CurrentWeather>?,

    )
