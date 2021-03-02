package com.calamity.weather.data.api.openweather.subclasses

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class WeatherCondition(
    @SerializedName("id") var id: Int,
    @SerializedName("main") var main: String,
    @SerializedName("description") var description: String,
    @SerializedName("icon") var icon: String
) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
