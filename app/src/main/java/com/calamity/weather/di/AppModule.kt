package com.calamity.weather.di

import android.app.Application
import androidx.room.Room
import com.calamity.weather.data.database.AutocompleteDatabase
import com.calamity.weather.data.database.WeatherDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(
        app: Application,
        callback: WeatherDatabase.Callback
    ) = Room.databaseBuilder(app, WeatherDatabase::class.java, "weather_database")
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Provides
    @Singleton
    fun providePredictionsDatabase(
        app: Application,
        callback: AutocompleteDatabase.Callback
    ) = Room.databaseBuilder(app, AutocompleteDatabase::class.java, "autocomplete_database")
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Provides
    fun provideWeatherDao(db: WeatherDatabase) = db.weatherDao()
    @Provides
    fun provideCurrentWeatherDao(db: WeatherDatabase) = db.currentWeatherDao()
    @Provides
    fun provideAutocompleteDao(db: AutocompleteDatabase) = db.dao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
