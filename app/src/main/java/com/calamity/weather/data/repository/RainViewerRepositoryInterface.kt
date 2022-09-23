package com.calamity.weather.data.repository

import com.calamity.weather.data.api.rainviewer.RainviewerRoot

// This interface is needed to create a fake repository valid for the corresponding view model
interface RainViewerRepositoryInterface {
    suspend fun getInfo(onResponseListener: (RainviewerRoot) -> Unit)
}