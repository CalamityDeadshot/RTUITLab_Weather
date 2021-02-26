package com.calamity.weather.data.api

import com.google.gson.annotations.SerializedName

data class ApiResponseWrapper(@SerializedName("message") var message: String,
                              @SerializedName("list") var responseList: List<CurrentWeather>
)
