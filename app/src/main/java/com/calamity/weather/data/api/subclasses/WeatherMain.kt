package com.calamity.weather.data.api.subclasses

import com.google.gson.annotations.SerializedName

data class WeatherMain(
    @SerializedName("temp") var temp: Double?,
    @SerializedName("feels_like") var feelsLike: Double,
    @SerializedName("temp_min") var minTemp: Double,
    @SerializedName("temp_max") var maxTemp: Double,
    @SerializedName("pressure") var pressure: Int,
    @SerializedName("humidity") var humidity: Int,
) {
}
