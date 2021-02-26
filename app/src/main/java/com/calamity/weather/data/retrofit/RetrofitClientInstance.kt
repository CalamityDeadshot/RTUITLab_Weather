package com.calamity.weather.data.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitClientInstance {

    companion object {
        private var retrofit: Retrofit? = null
        private val BASE_URL = "https://api.openweathermap.org/data/2.5/"
        val API_KEY = "acf9cdac3b6bc576f123ce8c2ba69136"
        fun getRetrofitInstance(): Retrofit? {
            // If retrofit instance is null, build it
            return retrofit ?: Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}