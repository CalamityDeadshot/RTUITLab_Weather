package com.calamity.weather.data.api.openweather.subclasses

import com.google.gson.annotations.SerializedName

data class WeatherSystem(
    @SerializedName("country") var countryCode: String,
    @SerializedName("sunrise") var sunriseTime: Int,
    @SerializedName("sunset") var sunsetTime: Int
)
