package com.calamity.weather.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.data.api.openweather.subclasses.WeatherCondition
import com.calamity.weather.data.api.openweather.subclasses.onecall.CurrentWeather
import com.calamity.weather.data.api.openweather.subclasses.onecall.HourlyWeather
import com.calamity.weather.data.database.WeatherDatabase
import com.calamity.weather.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class WeatherDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: WeatherDatabase
    private lateinit var dao: WeatherDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.weatherDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun insert() = runBlockingTest {
        val weatherItem = Weather(0.0, 0.0, "", 0,
            CurrentWeather(0, 0, 0, 0.0, 0.0, 0.0, 0,
                0.0F, 0, 0.0F, 0.0F,
                listOf(WeatherCondition(0, "", "", "")),
            null),
            listOf(),
            listOf(),
            false, id = 1
        )
        dao.insert(weatherItem)
        val items = dao.getWeather().asLiveData().getOrAwaitValue()

        assertThat(items).contains(weatherItem)
    }

    @Test
    fun delete() = runBlockingTest {
        val weatherItem = Weather(0.0, 0.0, "", 0,
            CurrentWeather(0, 0, 0, 0.0, 0.0, 0.0, 0,
                0.0F, 0, 0.0F, 0.0F,
                listOf(WeatherCondition(0, "", "", "")),
                null),
            listOf(),
            listOf(),
            false, id = 1
        )
        dao.insert(weatherItem)
        dao.delete(weatherItem)

        val items = dao.getWeather().asLiveData().getOrAwaitValue()

        assertThat(items).doesNotContain(weatherItem)
    }

    @Test
    fun update() = runBlockingTest {
        val weatherItemToBeUpdated = Weather(0.0, 0.0, "", 0,
            CurrentWeather(0, 0, 0, 0.0, 0.0, 0.0, 0,
                0.0F, 0, 0.0F, 0.0F,
                listOf(WeatherCondition(0, "", "", "")),
                null),
            listOf(),
            listOf(),
            false, id = 1
        )
        val newWeatherItem = Weather(0.0, 0.0, "timezone", 0,
            CurrentWeather(0, 0, 0, 0.0, 0.0, 0.0, 0,
                0.0F, 0, 0.0F, 0.0F,
                listOf(WeatherCondition(1, "new", "smh", "")),
                null),
            listOf(),
            listOf(),
            false, id = 1
        )
        dao.insert(weatherItemToBeUpdated)
        dao.update(newWeatherItem)

        assertThat(dao.getWeatherById(1)).isEqualTo(newWeatherItem)
    }

    @Test
    fun getWeatherById() = runBlockingTest {
        val weatherItem = Weather(0.0, 0.0, "", 0,
            CurrentWeather(0, 0, 0, 0.0, 0.0, 0.0, 0,
                0.0F, 0, 0.0F, 0.0F,
                listOf(WeatherCondition(0, "", "", "")),
                null),
            listOf(),
            listOf(),
            false, id = 1
        )
        dao.insert(weatherItem)

        assertThat(dao.getWeatherById(1)).isEqualTo(weatherItem)
    }

    @Test
    fun getWeatherAsList() = runBlockingTest {
        val weatherItem1 = Weather(0.0, 0.0, "", 0,
            CurrentWeather(0, 0, 0, 0.0, 0.0, 0.0, 0,
                0.0F, 0, 0.0F, 0.0F,
                listOf(WeatherCondition(0, "", "", "")),
                null),
            listOf(),
            listOf(),
            false, id = 1
        )
        val weatherItem2 = Weather(1.0, 3.0, "ASD", 125,
            CurrentWeather(0, 0, 0, 0.0, 0.0, 0.0, 0,
                0.0F, 0, 0.0F, 0.0F,
                listOf(WeatherCondition(0, "", "", "")),
                null),
            listOf(),
            listOf(),
            false, id = 2
        )
        dao.insert(weatherItem1)
        dao.insert(weatherItem2)

        assertThat(dao.getWeatherAsList()).isEqualTo(listOf(weatherItem1, weatherItem2))
    }

    @Test
    fun getWeatherByName() = runBlockingTest {
        val weatherItem = Weather(0.0, 0.0, "", 0,
            CurrentWeather(0, 0, 0, 0.0, 0.0, 0.0, 0,
                0.0F, 0, 0.0F, 0.0F,
                listOf(WeatherCondition(0, "", "", "")),
                null),
            listOf(),
            listOf(),
            false, id = 1,
            cityName = "uwu"
        )
        dao.insert(weatherItem)
        assertThat(dao.getWeather("uwu").asLiveData().getOrAwaitValue()).isEqualTo(listOf(weatherItem))
    }

}