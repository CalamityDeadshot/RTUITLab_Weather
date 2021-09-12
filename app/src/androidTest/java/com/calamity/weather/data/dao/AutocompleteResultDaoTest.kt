package com.calamity.weather.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.calamity.weather.data.api.places.PlacesPrediction
import com.calamity.weather.data.database.AutocompleteDatabase
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
class AutocompleteResultDaoTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AutocompleteDatabase
    private lateinit var dao: AutocompleteResultDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AutocompleteDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.dao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun insert() = runBlockingTest {
        val prediction = PlacesPrediction("smh", 0.0, 0.0, "some id")
        dao.insert(prediction)
        assertThat(dao.getPredictionsAsList()).contains(prediction)
    }

    @Test
    fun update() = runBlockingTest {
        val prediction = PlacesPrediction("smh", 0.0, 0.0, "some id")
        val predictionNew = PlacesPrediction("smh else", 0.0, 0.0, "some id")
        dao.insert(prediction)
        dao.update(predictionNew)
        assertThat(dao.getPredictionsAsList()).contains(predictionNew)
        assertThat(dao.getPredictionsAsList()).doesNotContain(prediction)
    }

    @Test
    fun delete() = runBlockingTest {
        val prediction = PlacesPrediction("smh", 0.0, 0.0, "some id")
        dao.insert(prediction)
        dao.delete(prediction)

        assertThat(dao.getPredictionsAsList()).doesNotContain(prediction)
    }

    @Test
    fun getDataCount() = runBlockingTest {
        val prediction1 = PlacesPrediction("smh1", 0.0, 0.0, "some id 1")
        val prediction2 = PlacesPrediction("smh2", 0.0, 0.0, "some id 2")
        val prediction3 = PlacesPrediction("smh3", 0.0, 0.0, "some id 3")
        dao.insert(prediction1)
        dao.insert(prediction2)
        dao.insert(prediction3)

        assertThat(dao.getDataCount()).isEqualTo(3)
    }

    @Test
    fun getPredictionsByName() = runBlockingTest {
        val prediction = PlacesPrediction("smh name", 0.0, 0.0, "some id")
        dao.insert(prediction)

        assertThat(dao.getPredictions("smh name").asLiveData().getOrAwaitValue()).contains(prediction)
    }
}