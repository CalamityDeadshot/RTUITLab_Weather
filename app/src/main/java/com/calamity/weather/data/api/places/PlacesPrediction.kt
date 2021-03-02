package com.calamity.weather.data.api.places

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "autocomplete_table")
data class PlacesPrediction(
    val fullText: String,
    val latitude: Double,
    val longitude: Double,
    val placeId: String,
    var isAdded: Boolean = false,
    @PrimaryKey(autoGenerate = true) var id: Int = 0
) {


}
