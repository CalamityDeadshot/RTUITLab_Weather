package com.calamity.weather.data.retrofit.rainviewer

import com.calamity.weather.data.api.rainviewer.RainviewerRoot
import retrofit2.Call
import retrofit2.http.GET

interface RainViewerService {
    @GET("weather-maps.json")
    fun getPrecipitationInfo() : Call<RainviewerRoot>
}