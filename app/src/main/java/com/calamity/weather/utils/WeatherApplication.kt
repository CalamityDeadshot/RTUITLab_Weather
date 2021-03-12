package com.calamity.weather.utils

import android.app.Application
import com.calamity.weather.R
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WeatherApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NetworkMonitor(this).startNetworkCallback()
        Variables.languageCode = getString(R.string.language_code)
    }

    override fun onTerminate() {
        super.onTerminate()
        NetworkMonitor(this).stopNetworkCallback()
    }

}