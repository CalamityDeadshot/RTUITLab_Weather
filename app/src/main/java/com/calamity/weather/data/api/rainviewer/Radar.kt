package com.calamity.weather.data.api.rainviewer

data class Radar(
    val past: List<Frame>,
    val nowcast: List<Frame>
)
