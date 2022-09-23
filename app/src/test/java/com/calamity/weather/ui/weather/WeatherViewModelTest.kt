package com.calamity.weather.ui.weather

import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.data.api.openweather.subclasses.WeatherCondition
import com.calamity.weather.data.repository.FakeWeatherRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
class WeatherViewModelTest {
    private lateinit var viewModel: WeatherViewModel
    private lateinit var repository: FakeWeatherRepository

    @Before
    fun setup() {
        repository = FakeWeatherRepository()
        viewModel = WeatherViewModel(repository)
    }

    @Test
    fun onEntrySwiped() {
        val item = Weather(
            .0, .0,
            "", 0,
            com.calamity.weather.data.api.openweather.subclasses.onecall.CurrentWeather(
                0, 0, 0, 0.0, 0.0, 0.0, 0,
                0.0F, 0, 0.0F, 0.0F,
                listOf(WeatherCondition(0, "", "", "")),
                null
            ),
            listOf(),
            listOf(),
            false, id = 1
        )
        runBlockingTest {
            repository.insert(item)
        }
        viewModel.onEntrySwiped(item)
    }
}