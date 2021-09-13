package com.calamity.weather.data.retrofit.openweather

import com.calamity.weather.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class OpenweatherRetrofitClientInstance {

    companion object {
        private var retrofit: Retrofit? = null
        private val BASE_URL = "https://api.openweathermap.org/data/2.5/"
        val API_KEY: String = BuildConfig.OPENWEATHER_API_KEY
        fun getRetrofitInstance(): Retrofit? {
            // If retrofit instance is null, build it
            return retrofit ?: Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}