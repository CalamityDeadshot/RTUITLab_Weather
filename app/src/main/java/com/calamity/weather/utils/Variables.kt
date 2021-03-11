package com.calamity.weather.utils

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import kotlin.properties.Delegates

object Variables {
    var isNetworkConnected: Boolean by Delegates.observable(false) { property, oldValue, newValue ->
        Log.i("Network connectivity", "$newValue")
        isNetworkConnectedLive.postValue(isNetworkConnected)
    }

    val isNetworkConnectedLive = MutableLiveData<Boolean>()

    var languageCode: String = "en"

    var units: String = "metric"

    var exclude: String = "minutely"

    const val googleMapsUrl = "https://www.google.com/maps/@?api=1&map_action=map"
    const val yandexMapsUrl = "yandexmaps://maps.yandex.com/?"

    const val googleApiKey = "AIzaSyCoOd_Nq07amOuqQlO14Dm3C__DUW9wz84"
}