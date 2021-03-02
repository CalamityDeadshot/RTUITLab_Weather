package com.calamity.weather.data.api.openweather.subclasses

import com.google.gson.annotations.SerializedName

data class Clouds(@SerializedName("all") var cloudiness: Double)
