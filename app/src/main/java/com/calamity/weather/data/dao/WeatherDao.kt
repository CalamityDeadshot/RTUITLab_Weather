package com.calamity.weather.data.dao

import androidx.room.*
import com.calamity.weather.data.api.CurrentWeather
import com.calamity.weather.data.api.Weather
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather")
    fun getWeather(): Flow<List<Weather>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weather: Weather)

    @Update
    suspend fun update(weather: Weather)

    @Delete
    suspend fun delete(weather: Weather)
}