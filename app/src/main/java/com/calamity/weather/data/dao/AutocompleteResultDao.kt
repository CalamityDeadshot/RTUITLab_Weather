package com.calamity.weather.data.dao

import androidx.room.*
import com.calamity.weather.data.api.openweather.CurrentWeather
import com.calamity.weather.data.api.places.PlacesPrediction
import kotlinx.coroutines.flow.Flow

@Dao
interface AutocompleteResultDao {

    @Query("SELECT * FROM autocomplete_table WHERE (fullText LIKE '%' || :searchQuery || '%') AND (:searchQuery != '') ORDER BY id ASC")
    fun getPredictions(searchQuery: String) : Flow<List<PlacesPrediction>>
    @Query("SELECT * FROM autocomplete_table")
    fun getPredictionsAsList() : List<PlacesPrediction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prediction: PlacesPrediction)

    @Update
    suspend fun update(prediction: PlacesPrediction)

    @Delete
    suspend fun delete(prediction: PlacesPrediction)

    @Query("DELETE FROM autocomplete_table")
    fun nuke()
}