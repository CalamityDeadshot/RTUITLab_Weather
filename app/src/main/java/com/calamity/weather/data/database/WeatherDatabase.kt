package com.calamity.weather.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.calamity.weather.data.api.CurrentWeather
import com.calamity.weather.data.api.Weather
import com.calamity.weather.data.api.subclasses.*
import com.calamity.weather.data.dao.CurrentWeatherDao
import com.calamity.weather.data.dao.WeatherDao
import com.calamity.weather.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [CurrentWeather::class, Weather::class], version = 1)
@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao
    abstract fun currentWeatherDao(): CurrentWeatherDao

    class Callback @Inject constructor(
        private val database: Provider<WeatherDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        // First db initialization, todo: implement gps
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().currentWeatherDao()

            applicationScope.launch {
                dao.insert(CurrentWeather(0,
                        Coord(55.7558, 37.6173),
                        listOf(WeatherCondition(id=701, main="Mist", description="mist", icon="50d")),
                        WeatherMain(temp=3.84, feelsLike=-2.41, minTemp=3.0, maxTemp=4.44, pressure=1004, humidity=100),
                        0,
                        Wind(speed=7.0, deg=230),
                        Clouds(cloudiness=90.0),
                        1614319751,
                        WeatherSystem(countryCode="RU", sunriseTime=0, sunsetTime=0),
                        0, 524901, "Moscow"
                    )
                )
            }
        }
    }
}