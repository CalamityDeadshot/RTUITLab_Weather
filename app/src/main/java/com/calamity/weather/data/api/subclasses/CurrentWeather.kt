package com.calamity.weather.data.api.subclasses

import com.google.gson.annotations.SerializedName

data class CurrentWeather(
    @SerializedName("dt") var currentTime: Int,
    @SerializedName("sunrise") var sunriseTime: Int,
    @SerializedName("sunset") var sunsetTime: Int,
    @SerializedName("temp") var temperature: Double,
    @SerializedName("feels_like") var feelsLike: Double,
    @SerializedName("dew_point") var dewPoint: Double,
    @SerializedName("clouds") var cloudiness: Int,
    @SerializedName("uvi") var uvIndex: Int,
    @SerializedName("visibility") var visibility: Int,
    @SerializedName("wind_speed") var windSpeed: Int,
    @SerializedName("wind_deg") var windDirection: Int,
    @SerializedName("weather") var weatherCondition: WeatherCondition,
    @SerializedName("pop") var precipitationProbability: Int?,
    @SerializedName("hourly") var hourly: Array<CurrentWeather>?,
    @SerializedName("daily") var daily: Array<CurrentWeather>?,
    @SerializedName("minutely") var minutely: Array<CurrentWeather>?,

    )
