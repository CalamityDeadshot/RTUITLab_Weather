package com.calamity.weather.data.repository

import android.util.Log
import androidx.lifecycle.asLiveData
import com.calamity.weather.data.api.CurrentWeather
import com.calamity.weather.data.api.Weather
import com.calamity.weather.data.database.WeatherDatabase
import com.calamity.weather.data.retrofit.GetDataService
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

    suspend fun refreshCurrentWeather() {
        withContext(Dispatchers.IO) {
            val service = RetrofitClientInstance.getRetrofitInstance()!!.create(GetDataService::class.java)
            val currentDao = database.currentWeatherDao()
            currentDao.getCurrentWeatherAsList().toList().forEach { weather ->
                var w: CurrentWeather
                service.getCurrentWeather(RetrofitClientInstance.API_KEY, weather.cityId, "metric", "en")
                    .enqueue(object : retrofit2.Callback<CurrentWeather> {
                        override fun onResponse(
                            call: Call<CurrentWeather>,
                            response: Response<CurrentWeather>
                        ) {
                            w = response.body()!!
                            w.db_id = weather.db_id
                            GlobalScope.launch {
                                currentDao.update(w)
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
            val service = RetrofitClientInstance.getRetrofitInstance()!!.create(GetDataService::class.java)
            service.getWeather(RetrofitClientInstance.API_KEY, lat, lon, "minutely", "metric", "en")
                    .enqueue(object : retrofit2.Callback<Weather> {
                        override fun onResponse(
                                call: Call<Weather>,
                                response: Response<Weather>
                        ) {
                           GlobalScope.launch { database.weatherDao().insert(response.body()!!) }
                        }

                        override fun onFailure(call: Call<Weather>, t: Throwable) {
                            Log.v("retrofit", "call failed: ${t.message}")
                        }
                    })
        }
    }

    fun getCurrentWeather() = database.currentWeatherDao().getCurrentWeather().asLiveData()

    fun getWeather() = database.weatherDao().getWeather().asLiveData()
}