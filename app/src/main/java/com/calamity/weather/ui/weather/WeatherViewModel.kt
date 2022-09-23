package com.calamity.weather.ui.weather

import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.data.repository.WeatherRepository
import com.calamity.weather.data.repository.WeatherRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor (
    private val repository: WeatherRepositoryInterface
) : ViewModel(){

    val searchQuery = MutableStateFlow("")

    private val weatherFlow = searchQuery.flatMapLatest {
        repository.getWeather(it)
    }

    val weather = weatherFlow.asLiveData()

    private val observer = Observer<List<Weather>> {
        Log.v("ViewModel", "observed ${busy.value}")
        busy.value = false
    }

    init {
        weather.observeForever(observer)
    }
    var busy = MutableLiveData<Boolean>(false)
    suspend fun update() {
        busy.value = true
        repository.refreshWeather()
    }

    suspend fun addGpsWeather(lat: Double, lon: Double) {
        busy.value = true
        repository.getWeatherByLocation(lat, lon) {
            busy.postValue(false)
        }
    }

    fun onEntrySwiped(weather: Weather) = viewModelScope.launch {
        repository.delete(weather)
        eventChannel.send(WeatherEvent.ShowUndoDeleteMessage(weather))
    }

    fun onUndoDelete(weather: Weather) = viewModelScope.launch {
        repository.insert(weather)
    }

    private val eventChannel = Channel<WeatherEvent>()
    val event = eventChannel.receiveAsFlow()


    fun getWeatherById(id: Int, callback: (Weather) -> Unit) = viewModelScope.launch {
        repository.getWeatherById(id) {
            callback(it)
        }
    }

    fun onNotificationAction(entry: Weather, isSet: Boolean) = viewModelScope.launch {
        repository.update(entry, isSet)
    }

    sealed class WeatherEvent {
        data class ShowUndoDeleteMessage(val weather: Weather) : WeatherEvent()
    }

    override fun onCleared() {
        weather.removeObserver(observer)
        super.onCleared()
    }
}