package com.calamity.weather.data.api.subclasses

import com.google.gson.annotations.SerializedName

data class Clouds(@SerializedName("all") var cloudiness: Double)
