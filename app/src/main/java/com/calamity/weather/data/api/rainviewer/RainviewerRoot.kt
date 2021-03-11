package com.calamity.weather.data.api.rainviewer

import com.google.gson.annotations.SerializedName

data class RainviewerRoot(
    @SerializedName("version") val apiVersion: String,
    @SerializedName("generated") val timeGenerated: Long,
    val host: String,
    val radar: Radar,
    val satellite: Satellite
)
