package com.calamity.weather.data.dao

import androidx.room.*
import com.calamity.weather.data.api.places.PlacesPrediction
import kotlinx.coroutines.flow.Flow

@Dao
interface AutocompleteResultDao {

    @Query("SELECT * FROM autocomplete_table WHERE (fullText LIKE '%' || :searchQuery || '%') AND (:searchQuery != '') ")
    fun getPredictions(searchQuery: String) : Flow<List<PlacesPrediction>>
    @Query("SELECT * FROM autocomplete_table")
    suspend fun getPredictionsAsList() : List<PlacesPrediction>
    @Query("SELECT * FROM autocomplete_table WHERE (fullText LIKE '%' || :searchQuery || '%') AND (:searchQuery != '') ")
    suspend fun getPredictionsAsList(searchQuery: String) : List<PlacesPrediction>

    @Query("SELECT COUNT() FROM autocomplete_table")
    suspend fun getDataCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prediction: PlacesPrediction)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(prediction: PlacesPrediction)

    @Delete
    suspend fun delete(prediction: PlacesPrediction)

    @Query("DELETE FROM autocomplete_table")
    fun nuke()
}