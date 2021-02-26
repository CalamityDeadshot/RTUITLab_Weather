package com.calamity.weather.ui.detailedweather

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.calamity.weather.data.dao.WeatherDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
        private val weatherDao: WeatherDao
) : ViewModel() {
}