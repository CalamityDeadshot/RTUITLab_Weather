package com.calamity.weather.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.calamity.weather.R
import com.calamity.weather.data.api.ApiResponseWrapper
import com.calamity.weather.data.retrofit.GetDataService
import com.calamity.weather.data.retrofit.RetrofitClientInstance
import com.calamity.weather.ui.detailedweather.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val service = RetrofitClientInstance.getRetrofitInstance()!!.create(GetDataService::class.java)
        val call = service.getCurrentWeather(RetrofitClientInstance.API_KEY, "Moscow,129,ru", "metric", "en")
        call.enqueue(object : Callback<ApiResponseWrapper> {
            override fun onResponse(
                call: Call<ApiResponseWrapper>,
                response: Response<ApiResponseWrapper>
            ) {
                println(response.body().toString())
                println("${response.body()!!.responseList[0].cityName} : ${response.body()!!.responseList[0].main.temp}°C")
            }

            override fun onFailure(call: Call<ApiResponseWrapper>, t: Throwable) {
                Log.v("retrofit", "call failed: ${t.message}")
            }

        })
    }
}