package com.calamity.weather.data.repository

import android.util.Log
import com.calamity.weather.data.api.openweather.CurrentWeather
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.data.api.places.PlacesPrediction
import com.calamity.weather.data.database.WeatherDatabase
import com.calamity.weather.data.retrofit.openweather.WeatherService
import com.calamity.weather.data.retrofit.openweather.OpenweatherRetrofitClientInstance
import com.calamity.weather.utils.Variables
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
    private val service: WeatherService = OpenweatherRetrofitClientInstance.getRetrofitInstance()!!.create(
        WeatherService::class.java)

    // Methods belonging to current weather API call

    suspend fun refreshCurrentWeather() {
        withContext(Dispatchers.IO) {
            val currentDao = database.currentWeatherDao()
            currentDao.getCurrentWeatherAsList().toList().forEach { weather ->
                service.getCurrentWeather(OpenweatherRetrofitClientInstance.API_KEY, weather.cityId, Variables.units, Variables.languageCode)
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

    suspend fun getCurrentWeatherByLocation(lat: Double, lon: Double) {
        withContext(Dispatchers.IO) {
            service.getCurrentWeather(OpenweatherRetrofitClientInstance.API_KEY, lat, lon, Variables.units, Variables.languageCode)
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


    suspend fun addCurrentWeather(place: PlacesPrediction) {
        withContext(Dispatchers.IO) {
            service.getCurrentWeather(OpenweatherRetrofitClientInstance.API_KEY, place.latitude, place.longitude, Variables.units, Variables.languageCode)
                .enqueue{
                    onResponse = { response ->

                        GlobalScope.launch {
                            database.currentWeatherDao().insert(response.body()!!.apply { placeId = place.placeId })
                        }
                    }
                }
        }
    }

    suspend fun deleteCurrent(place: PlacesPrediction) {
        withContext(Dispatchers.IO) {
            for (weather in database.currentWeatherDao().getCurrentWeatherAsList()) {
                if (weather.placeId == place.placeId)
                    database.currentWeatherDao().delete(weather)
            }
        }
    }

    fun deleteCurrent(weather: CurrentWeather) = GlobalScope.launch {
        database.currentWeatherDao().delete(weather)
    }

    fun insertCurrent(weather: CurrentWeather) = GlobalScope.launch {
        database.currentWeatherDao().insert(weather)
    }

    fun getCurrentWeather(searchQuery: String) = database.currentWeatherDao().getCurrentWeather(searchQuery)


    // Methods belonging to OneCall API

    suspend fun refreshWeather() {
        withContext(Dispatchers.IO) {
            val dao = database.weatherDao()
            // Mock data
            /*if (dao.getWeatherAsList().isEmpty()) {
                for (i in -50..50 step(5)) {
                    dao.insert(
                        Weather(
                            30.0, 30.0,
                            "", 0,
                            com.calamity.weather.data.api.openweather.subclasses.onecall.CurrentWeather(
                                0, 0, 0, i.toDouble(), 0.0, 0.0, 0, .0F, 0, 0F, 0F,
                                listOf(), null
                            ),
                            listOf(), listOf(), false, cityName = "City Name $i"
                        )
                    )
                }
            }*/
            dao.getWeatherAsList().toList().forEach { weather ->
                service.getWeather(OpenweatherRetrofitClientInstance.API_KEY, weather.latitude, weather.longitude,Variables.exclude,Variables.units, Variables.languageCode)
                        .enqueue{
                            onResponse = { response ->
                                GlobalScope.launch {
                                    dao.update(response.body()!!.copy(
                                        id = weather.id,
                                        isLocationEntry = weather.isLocationEntry,
                                        placeId = weather.placeId,
                                        cityName = weather.cityName,
                                        cityId = weather.cityId
                                    ))
                                }
                            }
                        }
            }
        }
    }


    // This method is used only to add new weather entry; for refreshing entries see refreshWeather().
    // Openweather APIs do not allow getting location name when using OneCall API,
    // so to get full weather info by user's location we need two API calls:
    // first - to current weather, second - to OneCall
    suspend fun getWeatherByLocation(lat: Double, lon: Double, onFinish: () -> Unit) {
        withContext(Dispatchers.IO) {
            // First call current weather info to get name
            service.getCurrentWeather(OpenweatherRetrofitClientInstance.API_KEY, lat, lon, Variables.units, Variables.languageCode)
                .enqueue {
                    onResponse = { response ->

                        GlobalScope.launch {

                            val currentWeather = response.body()!!

                            // Determine if any location entry is already present in the list
                            var isNewLocationEntry = true
                            for (weather in database.weatherDao().getWeatherAsList()) {
                                if (weather.isLocationEntry) {
                                    // Determine if they are the same location
                                    val same = weather.cityId == currentWeather.cityId
                                    if (!same) {
                                        // If user changed their location, delete the old entry
                                        database.weatherDao().delete(weather)
                                    }
                                    isNewLocationEntry = !same
                                    break
                                }
                            }



                            // If user changed their location, add a new entry
                            if (isNewLocationEntry) {
                                // Then use OneCall to get full info
                                service.getWeather(OpenweatherRetrofitClientInstance.API_KEY, lat, lon, Variables.exclude, Variables.units, Variables.languageCode)
                                    .enqueue {
                                        onResponse = { oneCallResponse ->
                                            GlobalScope.launch {
                                                ensureLocationEntryIsSingle(oneCallResponse.body()!!)
                                                database.weatherDao().insert(
                                                    oneCallResponse.body()!!.apply { // Copy name and ID from current weather
                                                        isLocationEntry = true
                                                        cityName = currentWeather.cityName
                                                        cityId = currentWeather.cityId
                                                        placeId = "location_place"
                                                    }
                                                )
                                            }
                                        }
                                    }

                            }
                            onFinish.invoke()
                        }
                    }
                }
        }
    }

    private suspend fun ensureLocationEntryIsSingle(entry: Weather) {
        database.weatherDao().getWeatherAsList().forEach {
            if (it.placeId == entry.placeId) database.weatherDao().delete(it)
        }
    }

    // This method is used to add new entries from Search screen
    suspend fun addWeather(place: PlacesPrediction) {
        withContext(Dispatchers.IO) {
            service.getWeather(OpenweatherRetrofitClientInstance.API_KEY, place.latitude, place.longitude, Variables.exclude, Variables.units, Variables.languageCode)
                .enqueue{
                    onResponse = { response ->

                        GlobalScope.launch {
                            val w = response.body()!!.apply {
                                placeId = place.placeId
                                cityName = place.fullText.split(", ")[0]
                            }
                            Log.v("Add weather", w.toString())
                            database.weatherDao().insert(w)
                        }
                    }
                }
        }
    }

    suspend fun delete(place: PlacesPrediction) {
        withContext(Dispatchers.IO) {
            for (weather in database.weatherDao().getWeatherAsList()) {
                if (weather.placeId == place.placeId)
                    database.weatherDao().delete(weather)
            }
        }
    }

    fun delete(weather: Weather) = GlobalScope.launch {
        database.weatherDao().delete(weather)
    }

    fun insert(weather: Weather) = GlobalScope.launch {
        database.weatherDao().insert(weather)
    }


    fun getWeatherById(id: Int, callback: (Weather) -> Unit) = GlobalScope.launch {
        callback(database.weatherDao().getWeatherById(id))
    }

    fun getWeather() = database.weatherDao().getWeather()

    fun getWeather(searchQuery: String) = database.weatherDao().getWeather(searchQuery)

}