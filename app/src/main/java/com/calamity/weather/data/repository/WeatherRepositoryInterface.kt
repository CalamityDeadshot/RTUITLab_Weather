package com.calamity.weather.data.repository

import com.calamity.weather.data.api.openweather.CurrentWeather
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.data.api.places.PlacesPrediction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

// This interface is needed to create a fake repository valid for the corresponding view model
interface WeatherRepositoryInterface {
    suspend fun refreshCurrentWeather()

    suspend fun getCurrentWeatherByLocation(lat: Double, lon: Double)

    suspend fun addCurrentWeather(place: PlacesPrediction)

    suspend fun deleteCurrent(place: PlacesPrediction)

    fun deleteCurrent(weather: CurrentWeather): Job

    fun insertCurrent(weather: CurrentWeather): Job

    fun getCurrentWeather(searchQuery: String): Flow<List<CurrentWeather>>

    suspend fun refreshWeather()

    suspend fun getWeatherByLocation(lat: Double, lon: Double, onFinish: () -> Unit)

    suspend fun addWeather(place: PlacesPrediction)

    suspend fun delete(place: PlacesPrediction)

    suspend fun delete(weather: Weather)

    suspend fun insert(weather: Weather)

    suspend fun update(entry: Weather, notificationSet: Boolean)

    suspend fun getWeatherById(id: Int, callback: (Weather) -> Unit)

    fun getWeather(): Flow<List<Weather>>

    fun getWeather(searchQuery: String): Flow<List<Weather>>
}