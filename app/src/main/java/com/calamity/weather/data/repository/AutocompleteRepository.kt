package com.calamity.weather.data.repository

import com.calamity.weather.data.api.places.PlacesPrediction
import com.calamity.weather.data.database.AutocompleteDatabase
import com.calamity.weather.data.database.WeatherDatabase
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AutocompleteRepository @Inject constructor(
    private val database: AutocompleteDatabase,
    private val weatherDatabase: WeatherDatabase
) {
    private var client: PlacesClient? = null

    fun provideClient(client: PlacesClient) {
        this.client = this.client ?: client
    }


    suspend fun getPredictions(query: String) : Flow<List<PlacesPrediction>> {
        withContext(Dispatchers.IO) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setTypeFilter(TypeFilter.REGIONS)
                .build()


            client!!.findAutocompletePredictions(request).addOnSuccessListener { response ->
                for (prediction in response.autocompletePredictions) {
                    GlobalScope.launch {
                        val present = database.dao().getPredictionsAsList()

                        // Checking if a place with this id is already present
                        var needSecondApiCall = true
                        for (place in present) {
                            if (place.placeId == prediction.placeId) {
                                needSecondApiCall = false
                                break
                            }
                        }
                        // Skipping second API call if it is
                        if (needSecondApiCall) {

                            val coordinatesRequest = FetchPlaceRequest.newInstance(
                                prediction.placeId,
                                listOf(Place.Field.LAT_LNG)
                            )
                            client!!.fetchPlace(coordinatesRequest)
                                .addOnSuccessListener { response2: FetchPlaceResponse ->
                                    val coordinates = response2.place.latLng!!

                                    GlobalScope.launch {

                                        var isAdded = false

                                        for (weather in weatherDatabase.currentWeatherDao()
                                            .getCurrentWeatherAsList()) {
                                            if (weather.placeId == prediction.placeId) {
                                                isAdded = true
                                                break
                                            }
                                        }

                                        val v = PlacesPrediction(
                                            prediction.getFullText(null).toString(),
                                            coordinates.latitude,
                                            coordinates.longitude,
                                            prediction.placeId,
                                            isAdded
                                        )

                                        database.dao().insert(v)
                                    }
                                }
                        }
                    }
                }
            }
        }
        return database.dao().getPredictions(query)
    }

    suspend fun update(place: PlacesPrediction, added: Boolean) =
        database.dao().update(place.copy(isAdded = added))

    suspend fun clearDb() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
            database.dao().nuke()
        }
    }

    suspend fun getPredictionsAsList(query: String) = database.dao().getPredictionsAsList(query)
}