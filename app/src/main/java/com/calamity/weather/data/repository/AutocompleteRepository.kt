package com.calamity.weather.data.repository

import com.calamity.weather.data.api.places.PlacesPrediction
import com.calamity.weather.data.database.AutocompleteDatabase
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
    private val database: AutocompleteDatabase
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

            val present = database.dao().getPredictionsAsList()

            client!!.findAutocompletePredictions(request).addOnSuccessListener { response ->
                for (prediction in response.autocompletePredictions) {

                    // Checking if a place with this id is already present
                    var needSecondApiCall = true
                    for (place in present) {
                        if (place.placeId == prediction.placeId) {
                            needSecondApiCall = false
                            break
                        }
                    }
                    // Skipping second API call if it is
                    if (!needSecondApiCall) continue

                    val coordinatesRequest = FetchPlaceRequest.newInstance(prediction.placeId, listOf(Place.Field.LAT_LNG))
                    client!!.fetchPlace(coordinatesRequest)
                        .addOnSuccessListener { response2: FetchPlaceResponse ->
                            //val predictions = ArrayList<PlacesPrediction>()
                            val coordinates = response2.place.latLng!!
                            val text = prediction.getFullText(null)
                            val countryName = text.split(", ").last()
                            val cityName = text.split(", ")[0]
                            //predictions.add(PlacesPrediction(countryName, cityName, coordinates.latitude, coordinates.longitude))

                            val v = PlacesPrediction(prediction.getFullText(null).toString(), coordinates.latitude, coordinates.longitude, prediction.placeId)
                            GlobalScope.launch {
                                database.dao().insert(v)
                            }
                        }
                }
            }
        }
        return database.dao().getPredictions(query)
    }

    suspend fun clearDb() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
            database.dao().nuke()
        }
    }
}