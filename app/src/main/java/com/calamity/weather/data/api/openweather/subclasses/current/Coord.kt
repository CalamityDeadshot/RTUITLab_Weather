package com.calamity.weather.data.api.openweather.subclasses.current

import com.google.gson.annotations.SerializedName

data class Coord(@SerializedName("lon") var lon: Double,
                 @SerializedName("lat") var lat: Double)
{
}
