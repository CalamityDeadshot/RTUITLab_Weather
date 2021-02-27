package com.calamity.weather.data.database

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.calamity.weather.data.api.ApiResponseWrapper
import com.calamity.weather.data.api.CurrentWeather
import com.calamity.weather.data.api.Weather
import com.calamity.weather.data.api.subclasses.*
import com.calamity.weather.data.dao.CurrentWeatherDao
import com.calamity.weather.data.dao.WeatherDao
import com.calamity.weather.data.retrofit.GetDataService
import com.calamity.weather.data.retrofit.RetrofitClientInstance
import com.calamity.weather.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
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
            //val dao = database.get().weatherDao()

            /*applicationScope.launch {
                dao.insert(CurrentWeather(0,
                        Coord(55.7558, 37.6173),
                        listOf(WeatherCondition(id=701, main="Mist", description="mist", icon="50d"), WeatherCondition(id=701, main="Mist", description="mist", icon="50d")),
                        WeatherMain(temp=3.84, feelsLike=-2.41, minTemp=3.0, maxTemp=4.44, pressure=1004, humidity=100),
                        0,
                        Wind(speed=7.0, deg=230),
                        Clouds(cloudiness=90.0),
                        1614319751,
                        WeatherSystem(countryCode="RU", sunriseTime=0, sunsetTime=0),
                        0, 524901, "Moscow"
                    )
                )
            }*/

            val service = RetrofitClientInstance.getRetrofitInstance()!!.create(GetDataService::class.java)
            val call = service.getCurrentWeather(RetrofitClientInstance.API_KEY, "Moscow,129,ru", "metric", "en")
            call.enqueue(object : retrofit2.Callback<ApiResponseWrapper> {
                override fun onResponse(
                    call: Call<ApiResponseWrapper>,
                    response: Response<ApiResponseWrapper>
                ) {
                    applicationScope.launch {
                        dao.insert(response.body()!!.responseList[0])
                    }
                }

                override fun onFailure(call: Call<ApiResponseWrapper>, t: Throwable) {
                    Log.v("retrofit", "call failed: ${t.message}")
                }

            })
        }
    }
}