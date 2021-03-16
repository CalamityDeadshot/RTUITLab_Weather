package com.calamity.weather.ui.map

import android.content.res.Configuration
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.calamity.weather.R
import com.calamity.weather.data.api.openweather.Weather
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.UrlTileProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_precipitation_map.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL


@AndroidEntryPoint
class PrecipitationMapFragment : Fragment(R.layout.fragment_precipitation_map), OnMapReadyCallback {

    private val viewModel: MapViewModel by viewModels()

    var gMap: GoogleMap? = null
    var entryId: Int = -1
    lateinit var weather: Weather

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
        postponeEnterTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        entryId = arguments?.getInt("id")!!
        val transitionName = arguments?.getString("transitionName")!!
        map.transitionName = transitionName
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getWeatherById(entryId) {
                weather = it
                lifecycleScope.launch {
                    with(map) {
                        onCreate(null)
                        getMapAsync(this@PrecipitationMapFragment)
                    }
                }
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        MapsInitializer.initialize(requireContext())
        Log.v("Map", "onready")
        gMap = googleMap
        with(gMap!!.uiSettings) {
            isZoomControlsEnabled = true
            isCompassEnabled = false
            isMapToolbarEnabled = false
            isMyLocationButtonEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
        }
        map.onResume()
        setMapLocation()
        startPostponedEnterTransition()
    }
    private fun setMapLocation() {
        if (gMap == null) return
        with(gMap!!) {
            moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        weather.latitude,
                        weather.longitude
                    ), 10f
                )
            )
            mapType = GoogleMap.MAP_TYPE_NORMAL
            setOnMapClickListener {
                gMap!!.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            weather.latitude,
                            weather.longitude
                        ), 10f
                    )
                )
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getInfo { response ->
                gMap!!.addTileOverlay(
                    TileOverlayOptions().tileProvider(
                    object : UrlTileProvider(256, 256) {
                        override fun getTileUrl(x: Int, y: Int, zoom: Int): URL {
                            val s =
                                "${response.host}${response.radar.past.last().path}/256/$zoom/$x/$y/1/1_1.png"
                            return URL(s)
                        }
                    }
                ).transparency(.5F))
            }
        }
    }

}