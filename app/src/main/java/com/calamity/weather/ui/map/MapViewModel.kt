package com.calamity.weather.ui.map

import android.util.Log
import androidx.lifecycle.*
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.data.api.rainviewer.RainviewerRoot
import com.calamity.weather.data.repository.RainViewerRepository
import com.calamity.weather.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor (
    private val repository: RainViewerRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel(){
    suspend fun getInfo(onResponseListener: (RainviewerRoot) -> Unit) {
        repository.getInfo {
            onResponseListener(it)
        }
    }

    suspend fun getWeatherById(id: Int, callback: (Weather) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        weatherRepository.getWeatherById(id) {
            callback(it)
        }
    }

}