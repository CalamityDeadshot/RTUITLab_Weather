package com.calamity.weather.ui.currentweather

import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.calamity.weather.R
import com.calamity.weather.ui.detailedweather.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrentWeatherFragment : Fragment(R.layout.fragment_current_weather) {
    private val weatherViewModel: CurrentWeatherViewModel by viewModels()
}