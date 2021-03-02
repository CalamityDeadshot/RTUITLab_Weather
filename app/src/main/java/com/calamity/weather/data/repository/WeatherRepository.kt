package com.calamity.weather.data.repository

import android.util.Log
import com.calamity.weather.data.api.openweather.CurrentWeather
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.data.database.WeatherDatabase
import com.calamity.weather.data.retrofit.WeatherService
import com.calamity.weather.data.retrofit.RetrofitClientInstance
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

    suspend fun refreshWeather() {
        withContext(Dispatchers.IO) {
            val dao = database.weatherDao()
            dao.getWeatherAsList().toList().forEach { weather ->
                service.getWeather(RetrofitClientInstance.API_KEY, weather.latitude, weather.longitude,"minutely","metric", "en")
                        .enqueue(object : retrofit2.Callback<Weather> {
                            override fun onResponse(
                                call: Call<Weather>,
                                response: Response<Weather>
                            ) {
                                GlobalScope.launch {
                                    dao.update(response.body()!!.copy(id = weather.id))
                                }
                            }

                            override fun onFailure(call: Call<Weather>, t: Throwable) {
                                Log.v("retrofit", "call failed: ${t.message}")
                            }
                        })
            }
        }
    }

    suspend fun getWeatherByLocation(lat: Double, lon: Double) {
        withContext(Dispatchers.IO) {
            service.getCurrentWeather(RetrofitClientInstance.API_KEY, lat, lon, "metric", "en")
                    .enqueue(object : retrofit2.Callback<CurrentWeather> {
                        override fun onResponse(
                            call: Call<CurrentWeather>,
                            response: Response<CurrentWeather>
                        ) {
                           GlobalScope.launch {
                               database.currentWeatherDao().insert(
                                   response.body()!!.apply { isLocationEntry = true }
                               )
                           }
                        }

                        override fun onFailure(call: Call<CurrentWeather>, t: Throwable) {
                            Log.v("retrofit", "call failed: ${t.message}")
                        }
                    })
        }
    }

    fun getCurrentWeather(searchQuery: String) = database.currentWeatherDao().getCurrentWeather(searchQuery)

    fun getWeather() = database.weatherDao().getWeather()

    fun delete(weather: CurrentWeather) = GlobalScope.launch {
        database.currentWeatherDao().delete(weather)
    }

    fun insert(weather: CurrentWeather) = GlobalScope.launch {
        database.currentWeatherDao().insert(weather)
    }
}