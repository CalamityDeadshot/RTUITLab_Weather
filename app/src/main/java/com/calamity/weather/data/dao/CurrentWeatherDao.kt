package com.calamity.weather.data.dao

import androidx.room.*
import com.calamity.weather.data.api.CurrentWeather
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentWeatherDao {

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