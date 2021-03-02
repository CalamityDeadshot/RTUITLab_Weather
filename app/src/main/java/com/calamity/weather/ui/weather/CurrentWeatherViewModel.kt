package com.calamity.weather.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.calamity.weather.data.api.openweather.CurrentWeather
import com.calamity.weather.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrentWeatherViewModel @Inject constructor (
    private val repository: WeatherRepository
) : ViewModel(){

    val searchQuery = MutableStateFlow("")

    private val weatherFlow = searchQuery.flatMapLatest {
        repository.getCurrentWeather(it)
    }

    val weather = weatherFlow.asLiveData()

    suspend fun update() = repository.refreshCurrentWeather()

    suspend fun addGpsWeather(lat: Double, lon: Double) = repository.getWeatherByLocation(lat, lon)

    fun onEntrySwiped(weather: CurrentWeather) = viewModelScope.launch {
        repository.delete(weather)
        eventChannel.send(WeatherEvent.ShowUndoDeleteMessage(weather))
    }

    fun onUndoDelete(weather: CurrentWeather) = viewModelScope.launch {
        repository.insert(weather)
    }

    private val eventChannel = Channel<WeatherEvent>()
    val event = eventChannel.receiveAsFlow()

    sealed class WeatherEvent {
        data class ShowUndoDeleteMessage(val weather: CurrentWeather) : WeatherEvent()
    }
}