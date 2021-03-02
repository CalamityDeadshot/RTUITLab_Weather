package com.calamity.weather.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.calamity.weather.data.api.places.PlacesPrediction
import com.calamity.weather.data.dao.AutocompleteResultDao
import com.calamity.weather.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [PlacesPrediction::class], version = 1)
abstract class AutocompleteDatabase : RoomDatabase() {
    abstract fun dao(): AutocompleteResultDao

    class Callback @Inject constructor(
        private val database: Provider<AutocompleteDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

    }
}