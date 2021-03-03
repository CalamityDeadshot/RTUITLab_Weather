package com.calamity.weather.data.dao

import androidx.room.*
import com.calamity.weather.data.api.openweather.CurrentWeather
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentWeatherDao {

    @Query("SELECT * FROM current_weather WHERE cityName LIKE '%' || :searchQuery || '%' ORDER BY isLocationEntry DESC, db_id DESC")
    fun getCurrentWeather(searchQuery: String): Flow<List<CurrentWeather>>
    @Query("SELECT * FROM current_weather")
    fun getCurrentWeather(): Flow<List<CurrentWeather>>

    @Query("SELECT * FROM current_weather")
    fun getCurrentWeatherAsList(): List<CurrentWeather>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weather: CurrentWeather)

    @Update
    suspend fun update(weather: CurrentWeather)

    @Delete
    suspend fun delete(weather: CurrentWeather)

}