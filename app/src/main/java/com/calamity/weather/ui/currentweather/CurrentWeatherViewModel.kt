package com.calamity.weather.ui.currentweather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.calamity.weather.data.dao.CurrentWeatherDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CurrentWeatherViewModel @Inject constructor (
    private val currentWeatherDao: CurrentWeatherDao
) : ViewModel(){
    val weather = currentWeatherDao.getCurrentWeather().asLiveData()
}