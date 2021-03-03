package com.calamity.weather.data.api.openweather.subclasses.current

import com.google.gson.annotations.SerializedName

data class Clouds(@SerializedName("all") var cloudiness: Double)
