package com.calamity.weather.data.repository

import com.calamity.weather.data.api.rainviewer.RainviewerRoot

class FakeRainViewerRepository : RainViewerRepositoryInterface {



    override suspend fun getInfo(onResponseListener: (RainviewerRoot) -> Unit) {
        TODO("Not yet implemented")
    }
}