package com.calamity.weather.data.repository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.calamity.weather.data.api.openweather.CurrentWeather
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.data.api.openweather.subclasses.WeatherCondition
import com.calamity.weather.data.api.places.PlacesPrediction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class FakeWeatherRepository : WeatherRepositoryInterface {

    private val weatherItems = mutableListOf<Weather>()
    private val observableWeatherItems = MutableLiveData<List<Weather>>(weatherItems)
    private var networkAvailable = false

    fun setNetworkAvailable(value: Boolean) {
        networkAvailable = value
    }

    private fun refreshFlow() {
        observableWeatherItems.postValue(weatherItems)
    }

    override suspend fun refreshCurrentWeather() {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentWeatherByLocation(lat: Double, lon: Double) {
        TODO("Not yet implemented")
    }

    override suspend fun addCurrentWeather(place: PlacesPrediction) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCurrent(place: PlacesPrediction) {
        TODO("Not yet implemented")
    }

    override fun deleteCurrent(weather: CurrentWeather): Job {
        TODO("Not yet implemented")
    }

    override fun insertCurrent(weather: CurrentWeather): Job {
        TODO("Not yet implemented")
    }

    override fun getCurrentWeather(searchQuery: String): Flow<List<CurrentWeather>> {
        TODO("Not yet implemented")
    }

    // All methods above a not used in the current version
    override suspend fun refreshWeather() {
        TODO("Not yet implemented")
    }

    override suspend fun getWeatherByLocation(lat: Double, lon: Double, onFinish: () -> Unit) {
        weatherItems.add(Weather(
            lat, lon,
            "", 0,
            com.calamity.weather.data.api.openweather.subclasses.onecall.CurrentWeather(
                0, 0, 0, 0.0, 0.0, 0.0, 0,
                0.0F, 0, 0.0F, 0.0F,
                listOf(WeatherCondition(0, "", "", "")),
                null
            ),
            listOf(),
            listOf(),
            false, id = if (weatherItems.isEmpty()) 1 else weatherItems.last().id + 1
        ))
        onFinish.invoke()
        refreshFlow()
    }

    override suspend fun addWeather(place: PlacesPrediction) {
        weatherItems.add(Weather(
            place.latitude, place.longitude,
            "", 0,
            com.calamity.weather.data.api.openweather.subclasses.onecall.CurrentWeather(
                0, 0, 0, 0.0, 0.0, 0.0, 0,
                0.0F, 0, 0.0F, 0.0F,
                listOf(WeatherCondition(0, "", "", "")),
                null
            ),
            listOf(),
            listOf(),
            false, id = if (weatherItems.isEmpty()) 1 else weatherItems.last().id + 1, placeId = place.placeId,
            cityName = place.fullText.split(", ")[0]
        ))
        refreshFlow()
    }

    override suspend fun delete(place: PlacesPrediction) {
        for (weather in weatherItems) {
            if (weather.placeId == place.placeId)
                weatherItems.remove(weather)
        }
        refreshFlow()
    }

    override suspend fun delete(weather: Weather) {
        weatherItems.remove(weather)
        refreshFlow()
    }

    override suspend fun insert(weather: Weather) {
        weatherItems.add(weather)
        refreshFlow()
    }

    override suspend fun update(entry: Weather, notificationSet: Boolean) {
        weatherItems.forEachIndexed { i, it ->
            it.takeIf { it.id == entry.id }?.let {
                weatherItems[i] = entry
            }
        }
        refreshFlow()
    }

    override suspend fun getWeatherById(id: Int, callback: (Weather) -> Unit) {
        var item: Weather? = null
        weatherItems.forEach {
            item = it.takeIf { it.id == id }!!
        }
        callback.invoke(item!!)
        refreshFlow()
    }

    override fun getWeather(): Flow<List<Weather>> = observableWeatherItems.asFlow()

    override fun getWeather(searchQuery: String): Flow<List<Weather>> {
        val list = mutableListOf<Weather>()
        weatherItems.forEach {
            if (it.cityName.contains(searchQuery)) list.add(it)
        }
        return MutableLiveData<List<Weather>>(list).asFlow()
    }

}