package com.calamity.weather.data.api.subclasses

import com.google.gson.annotations.SerializedName

data class WeatherCondition(
    @SerializedName("id") var id: Int,
    @SerializedName("main") var main: String,
    @SerializedName("description") var description: String,
    @SerializedName("icon") var icon: String
) {
}
