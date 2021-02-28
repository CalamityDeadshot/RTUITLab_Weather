package com.calamity.weather.data.retrofit

import com.calamity.weather.data.api.ApiResponseWrapper
import com.calamity.weather.data.api.CurrentWeather
import com.calamity.weather.data.api.Weather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetDataService {

    @GET("find")
    fun getCurrentWeather(@Query("appid") apiKey: String,
                          @Query("q") cityName: String,
                          @Query("units") units: String,
                          @Query("lang") languageCode: String
    ): Call<ApiResponseWrapper>

    @GET("weather")
    fun getCurrentWeather(@Query("appid") apiKey: String,
                          @Query("id") cityId: Int,
                          @Query("units") units: String,
                          @Query("lang") languageCode: String
    ): Call<CurrentWeather>

    @GET("onecall")
    fun getWeather(@Query("appid") apiKey: String,
                   @Query("lat") latitude: Double,
                   @Query("lon") longitude: Double,
                   @Query("exclude") exclude: String,
                   @Query("units") units: String,
                   @Query("lang") languageCode: String
    ): Call<Weather>

}