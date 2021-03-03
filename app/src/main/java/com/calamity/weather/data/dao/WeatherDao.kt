package com.calamity.weather.data.dao

import androidx.room.*
import com.calamity.weather.data.api.openweather.Weather
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather WHERE cityName LIKE '%' || :searchQuery || '%' ORDER BY isLocationEntry DESC, id DESC")
    fun getWeather(searchQuery: String): Flow<List<Weather>>

    @Query("SELECT * FROM weather")
    fun getWeather(): Flow<List<Weather>>

    @Query("SELECT * FROM weather")
    fun getWeatherAsList(): List<Weather>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weather: Weather)

    @Update
    suspend fun update(weather: Weather)

    @Delete
    suspend fun delete(weather: Weather)
}