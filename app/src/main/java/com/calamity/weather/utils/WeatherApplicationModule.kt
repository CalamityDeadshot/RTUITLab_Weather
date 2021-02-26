package com.calamity.weather.utils

import com.calamity.weather.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


abstract class WeatherApplicationModule {

    abstract fun contributeActivityInjector(): MainActivity
}