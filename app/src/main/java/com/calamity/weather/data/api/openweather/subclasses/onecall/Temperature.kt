package com.calamity.weather.data.api.openweather.subclasses.onecall

import com.google.gson.annotations.SerializedName

data class Temperature(
    @SerializedName("morn") val morning: Float,
    @SerializedName("day") val day: Float,
    @SerializedName("eve") val eve: Float,
    @SerializedName("night") val night: Float,
    @SerializedName("min") val min: Float,
    @SerializedName("max") val max: Float
)
