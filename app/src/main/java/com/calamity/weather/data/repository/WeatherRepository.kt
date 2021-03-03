package com.calamity.weather.data.repository

import android.util.Log
import com.calamity.weather.data.api.openweather.CurrentWeather
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.data.api.places.PlacesPrediction
import com.calamity.weather.data.database.WeatherDatabase
import com.calamity.weather.data.retrofit.WeatherService
import com.calamity.weather.data.retrofit.RetrofitClientInstance
import com.calamity.weather.utils.enqueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val database: WeatherDatabase,
) {
    private val service: WeatherService = RetrofitClientInstance.getRetrofitInstance()!!.create(WeatherService::class.java)

    // Methods belonging to current weather API call

    suspend fun refreshCurrentWeather() {
        withContext(Dispatchers.IO) {
            val currentDao = database.currentWeatherDao()
            currentDao.getCurrentWeatherAsList().toList().forEach { weather ->
                service.getCurrentWeather(RetrofitClientInstance.API_KEY, weather.cityId, "metric", "en")
                    .enqueue(object : retrofit2.Callback<CurrentWeather> {
                        override fun onResponse(
                            call: Call<CurrentWeather>,
                            response: Response<CurrentWeather>
                        ) {
                            GlobalScope.launch {
                                currentDao.update(response.body()!!.copy(db_id = weather.db_id, isLocationEntry = weather.isLocationEntry))
                            }
                        }

                        override fun onFailure(call: Call<CurrentWeather>, t: Throwable) {
                            Log.v("retrofit", "call failed: ${t.message}")
                        }
                    })

            }
        }
    }

    suspend fun getWeatherByLocation(lat: Double, lon: Double) {
        withContext(Dispatchers.IO) {
            service.getCurrentWeather(RetrofitClientInstance.API_KEY, lat, lon, "metric", "en")
                .enqueue {
                    onResponse = { response ->

                        GlobalScope.launch {
                            var isNewLocationEntry = true
                            for (weather in database.currentWeatherDao().getCurrentWeatherAsList()) {
                                if (weather.isLocationEntry) {
                                    val same = weather.cityId == response.body()!!.cityId
                                    if (!same) {
                                        database.currentWeatherDao().delete(weather)
                                    }
                                    isNewLocationEntry = !same
                                    break
                                }
                            }

                            if (isNewLocationEntry)
                                database.currentWeatherDao().insert(
                                    response.body()!!.apply { isLocationEntry = true }
                                )
                        }
                    }
                }
        }
    }


    suspend fun addWeather(place: PlacesPrediction) {
        withContext(Dispatchers.IO) {
            service.getCurrentWeather(RetrofitClientInstance.API_KEY, place.latitude, place.longitude, "metric", "en")
                .enqueue{
                    onResponse = { response ->

                        GlobalScope.launch {
                            database.currentWeatherDao().insert(response.body()!!.apply { placeId = place.placeId })
                        }
                    }
                }
        }
    }

    suspend fun delete(place: PlacesPrediction) {
        withContext(Dispatchers.IO) {
            for (weather in database.currentWeatherDao().getCurrentWeatherAsList()) {
                if (weather.placeId == place.placeId)
                    database.currentWeatherDao().delete(weather)
            }
        }
    }

    fun delete(weather: CurrentWeather) = GlobalScope.launch {
        database.currentWeatherDao().delete(weather)
    }

    fun insert(weather: CurrentWeather) = GlobalScope.launch {
        database.currentWeatherDao().insert(weather)
    }

    fun getCurrentWeather(searchQuery: String) = database.currentWeatherDao().getCurrentWeather(searchQuery)


    // Methods belonging to OneCall API

    suspend fun refreshWeather() {
        withContext(Dispatchers.IO) {
            val dao = database.weatherDao()
            dao.getWeatherAsList().toList().forEach { weather ->
                service.getWeather(RetrofitClientInstance.API_KEY, weather.latitude, weather.longitude,"minutely","metric", "en")
                        .enqueue{
                            onResponse = { response ->
                                GlobalScope.launch {
                                    dao.update(response.body()!!.copy(id = weather.id))
                                }
                            }
                        }
            }
        }
    }



    fun getWeather() = database.weatherDao().getWeather()

}