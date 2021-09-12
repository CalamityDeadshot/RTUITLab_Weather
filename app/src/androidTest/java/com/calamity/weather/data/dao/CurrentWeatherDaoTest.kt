package com.calamity.weather.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.calamity.weather.data.api.openweather.CurrentWeather
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.data.api.openweather.subclasses.WeatherCondition
import com.calamity.weather.data.api.openweather.subclasses.current.*
import com.calamity.weather.data.database.WeatherDatabase
import com.calamity.weather.getOrAwaitValue
import com.google.common.truth.Truth
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
class CurrentWeatherDaoTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: WeatherDatabase
    private lateinit var dao: CurrentWeatherDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.currentWeatherDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun insert() = runBlockingTest {
        val weatherItem = CurrentWeather(1, Coord(.0, .0), listOf(),
            WeatherMain(null, .0, .0, .0, 0, 0),
            0,
            Wind(.0, 0),
            Clouds(.0),
            0,
            WeatherSystem("", 0, 0),
            0, 0, "", false
        )
        dao.insert(weatherItem)
        val items = dao.getCurrentWeather().asLiveData().getOrAwaitValue()

       assertThat(items).contains(weatherItem)
    }

    @Test
    fun delete() = runBlockingTest {
        val weatherItem = CurrentWeather(1, Coord(.0, .0), listOf(),
            WeatherMain(null, .0, .0, .0, 0, 0),
            0,
            Wind(.0, 0),
            Clouds(.0),
            0,
            WeatherSystem("", 0, 0),
            0, 0, "", false
        )
        dao.insert(weatherItem)
        dao.delete(weatherItem)
        val items = dao.getCurrentWeather().asLiveData().getOrAwaitValue()

        assertThat(items).doesNotContain(weatherItem)
    }

    @Test
    fun update() = runBlockingTest {
        val weatherItemToBeUpdated = CurrentWeather(1, Coord(.0, .0), listOf(),
            WeatherMain(null, .0, .0, .0, 0, 0),
            0,
            Wind(.0, 0),
            Clouds(.0),
            0,
            WeatherSystem("", 0, 0),
            0, 0, "", false
        )
        val newWeatherItem = CurrentWeather(1, Coord(312.0, 123.0), listOf(),
            WeatherMain(null, .0, .0, .0, 0, 0),
            13,
            Wind(.0, 0),
            Clouds(.0),
            0,
            WeatherSystem("ru", 0, 0),
            0, 0, "mukhosransk", false
        )
        dao.insert(weatherItemToBeUpdated)
        dao.update(newWeatherItem)

        assertThat(dao.getCurrentWeather().asLiveData().getOrAwaitValue()).contains(newWeatherItem)
    }

    @Test
    fun getWeatherByName() = runBlockingTest {
        val weatherItem = CurrentWeather(1, Coord(312.0, 123.0), listOf(),
            WeatherMain(null, .0, .0, .0, 0, 0),
            13,
            Wind(.0, 0),
            Clouds(.0),
            0,
            WeatherSystem("ru", 0, 0),
            0, 0, "mukhosransk", false
        )
        dao.insert(weatherItem)
        assertThat(dao.getCurrentWeather("mukhosransk").asLiveData().getOrAwaitValue()).isEqualTo(listOf(weatherItem))
    }

}