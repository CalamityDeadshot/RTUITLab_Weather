package com.calamity.weather.data.database

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.calamity.weather.data.api.openweather.ApiResponseWrapper
import com.calamity.weather.data.api.openweather.CurrentWeather
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.data.dao.CurrentWeatherDao
import com.calamity.weather.data.dao.WeatherDao
import com.calamity.weather.data.retrofit.WeatherService
import com.calamity.weather.data.retrofit.RetrofitClientInstance
import com.calamity.weather.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
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

        // First db initialization
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().currentWeatherDao()

            val service = RetrofitClientInstance.getRetrofitInstance()!!.create(WeatherService::class.java)
            service.getCurrentWeather(RetrofitClientInstance.API_KEY, "Moscow,129,ru", "metric", "en")
                .enqueue(object : retrofit2.Callback<ApiResponseWrapper> {
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

            service.getCurrentWeather(RetrofitClientInstance.API_KEY, "Omsk,644,ru", "metric", "en")
                .enqueue(object : retrofit2.Callback<ApiResponseWrapper> {
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

            service.getCurrentWeather(RetrofitClientInstance.API_KEY, "Arkhangelsk,163,ru", "metric", "en")
                .enqueue(object : retrofit2.Callback<ApiResponseWrapper> {
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