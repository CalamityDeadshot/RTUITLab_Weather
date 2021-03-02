package com.calamity.weather.ui.detailedweather

import androidx.lifecycle.ViewModel
import com.calamity.weather.data.dao.WeatherDao
import com.calamity.weather.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
        private val repository: WeatherRepository
) : ViewModel() {
    val weather = repository.getWeather()
}