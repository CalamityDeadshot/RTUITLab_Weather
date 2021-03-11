package com.calamity.weather.data.retrofit.rainviewer

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RainviewerRetrofitClientInstance {
    companion object {
        private var retrofit: Retrofit? = null
        private val BASE_URL = "https://api.rainviewer.com/public/"
        fun getRetrofitInstance(): Retrofit? {
            // If retrofit instance is null, build it
            return retrofit ?: Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}