package com.calamity.weather.utils

import android.util.Log
import kotlin.properties.Delegates

object Variables {
    var isNetworkConnected: Boolean by Delegates.observable(false) { property, oldValue, newValue ->
        Log.i("Network connectivity", "$newValue")
    }

    var languageCode: String = "en"

    var units: String = "metric"

    var exclude: String = "minutely"
}