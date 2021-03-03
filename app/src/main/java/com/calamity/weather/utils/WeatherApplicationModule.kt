package com.calamity.weather.utils

import com.calamity.weather.ui.mainactivity.MainActivity


abstract class WeatherApplicationModule {

    abstract fun contributeActivityInjector(): MainActivity
}