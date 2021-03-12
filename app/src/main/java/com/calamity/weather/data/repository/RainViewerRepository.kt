package com.calamity.weather.data.repository

import android.util.Log
import com.calamity.weather.data.api.rainviewer.RainviewerRoot
import com.calamity.weather.data.retrofit.rainviewer.RainViewerService
import com.calamity.weather.data.retrofit.rainviewer.RainviewerRetrofitClientInstance
import com.calamity.weather.utils.enqueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class RainViewerRepository @Inject constructor() {
    private val service: RainViewerService = RainviewerRetrofitClientInstance.getRetrofitInstance()!!.create(
        RainViewerService::class.java)

    suspend fun getInfo(onResponseListener: (RainviewerRoot) -> Unit) {
        withContext(Dispatchers.IO) {
            service.getPrecipitationInfo().enqueue {
                onResponse = { response ->
                    Log.v("Rainviewer", "response is null? ${response.body() == null}")
                    onResponseListener(response.body()!!)
                }
            }
        }
    }
}