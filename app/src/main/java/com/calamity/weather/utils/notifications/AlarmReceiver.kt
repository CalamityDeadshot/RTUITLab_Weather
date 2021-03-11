package com.calamity.weather.utils.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.calamity.weather.R
import com.calamity.weather.data.retrofit.openweather.OpenweatherRetrofitClientInstance
import com.calamity.weather.data.retrofit.openweather.WeatherService
import com.calamity.weather.utils.Variables
import com.calamity.weather.utils.enqueue
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class AlarmReceiver : BroadcastReceiver() {


    /**
     * Called when the BroadcastReceiver receives an Intent broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {

        // Deliver the notification.
        Log.v("Notifications", "Receiver received call")
        if (intent.extras == null) return
        val id = intent.getIntExtra(NotificationHelper.EXTRA_ID, -1)
        val interval = intent.getLongExtra(NotificationHelper.EXTRA_INTERVAL, -1)
        val latitude = intent.getDoubleExtra(NotificationHelper.EXTRA_LATITUDE, -1.0)
        val longitude = intent.getDoubleExtra(NotificationHelper.EXTRA_LONGITUDE, -1.0)
        val placeId = intent.getStringExtra(NotificationHelper.EXTRA_PLACE_ID)

        val async = goAsync()

        OpenweatherRetrofitClientInstance.getRetrofitInstance()!!.create(WeatherService::class.java)
            .getCurrentWeather(
                OpenweatherRetrofitClientInstance.API_KEY,
                latitude,
                longitude,
                Variables.units,
                Variables.languageCode
                )
            .enqueue {

                onResponse = {
                    val data = it.body()!!
                    //withContext(Dispatchers.Main) {}
                    NotificationHelper.createNotification(
                        context,
                        context.getString(R.string.weather_notification_heading, data.cityName),
                        context.getString(
                            R.string.weather_notification_text,
                            data.main.minTemp.roundToInt(),
                            data.main.maxTemp.roundToInt(),
                            data.main.feelsLike.roundToInt()
                        ),
                        context.getString(
                            R.string.weather_notification_text,
                            data.main.minTemp.roundToInt(),
                            data.main.maxTemp.roundToInt(),
                            data.main.feelsLike.roundToInt()
                        ),
                        "$placeId-${data.cityName}",
                        id
                    )

                    val pending =
                        PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    // Schedule notification

                    val nextNotificationTime = Calendar.getInstance().timeInMillis + interval

                    val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextNotificationTime, pending)
                    Log.v("Notifications", "Scheduled next notification id $id with start at " +
                            SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).format(nextNotificationTime)
                    )
                    async.finish()
                }


            }

    }

}