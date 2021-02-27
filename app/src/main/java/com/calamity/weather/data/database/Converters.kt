package com.calamity.weather.data.database

import androidx.room.TypeConverter
import com.calamity.weather.data.api.ApiResponseWrapper
import com.calamity.weather.data.api.Weather
import com.calamity.weather.data.api.subclasses.*
import com.google.gson.Gson


class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromApiResponseWrapper(value: ApiResponseWrapper?): String? = gson.toJson(value)
    @TypeConverter
    fun toApiResponseWrapper(value: String?): ApiResponseWrapper = gson.fromJson(
        value,
        ApiResponseWrapper::class.java
    )

    @TypeConverter
    fun coordToString(value: Coord?): String? = gson.toJson(value)
    @TypeConverter
    fun stringToCoord(value: String?): Coord = gson.fromJson(value, Coord::class.java)

    @TypeConverter
    fun fromWeatherCondition(value: WeatherCondition?): String = gson.toJson(value)
    @TypeConverter
    fun toWeatherCondition(value: String?): WeatherCondition = gson.fromJson(
        value,
        WeatherCondition::class.java
    )

    @TypeConverter
    fun fromWeatherMain(value: WeatherMain?): String = gson.toJson(value)
    @TypeConverter
    fun toWeatherMain(value: String?): WeatherMain = gson.fromJson(value, WeatherMain::class.java)

    @TypeConverter
    fun fromWind(value: Wind?): String = gson.toJson(value)
    @TypeConverter
    fun toWind(value: String?): Wind = gson.fromJson(value, Wind::class.java)

    @TypeConverter
    fun fromClouds(value: Clouds?): String = gson.toJson(value)
    @TypeConverter
    fun toClouds(value: String?): Clouds = gson.fromJson(value, Clouds::class.java)

    @TypeConverter
    fun fromWeatherSystem(value: WeatherSystem?): String = gson.toJson(value)
    @TypeConverter
    fun toWeatherSystem(value: String?): WeatherSystem = gson.fromJson(
        value,
        WeatherSystem::class.java
    )

    @TypeConverter
    fun fromWeather(value: Weather?): String = gson.toJson(value)
    @TypeConverter
    fun toWeather(value: String?): Weather = gson.fromJson(value, Weather::class.java)

    @TypeConverter
    fun fromCurrentWeather(value: CurrentWeather?): String = gson.toJson(value)
    @TypeConverter
    fun toCurrentWeather(value: String?): CurrentWeather = gson.fromJson(
        value,
        CurrentWeather::class.java
    )

    @TypeConverter
    fun fromWeatherConditions(value: List<WeatherCondition>): String = gson.toJson(value)
    @TypeConverter
    fun toWeatherConditions(value: String?): List<WeatherCondition> = gson.fromJson(value, Array<WeatherCondition>::class.java).toList()

}