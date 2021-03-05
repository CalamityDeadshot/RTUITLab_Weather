package com.calamity.weather.utils.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.calamity.weather.R
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.ui.mainactivity.MainActivity
import java.util.*
import kotlin.math.roundToInt


class NotificationHelper {
    companion object {

        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
        const val EXTRA_ID = "cityId"
        const val EXTRA_PLACE_ID = "placeId"

        fun createNotificationChannel(
            context: Context,
            importance: Int,
            showBadge: Boolean,
            data: Weather,
            description: String
        ) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                // Format: $placeId-$cityName
                val channelId = "${data.placeId}-${data.cityName}"
                val channel = NotificationChannel(channelId, "Weather for ${data.cityName}", importance)
                channel.description = description
                channel.setShowBadge(showBadge)

                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun createNotification(
            context: Context,
            title: String,
            message: String,
            bigText: String,
            channelId: String,
            cityId: Int,
            autoCancel: Boolean = true
        ) {

            //val channelId = "${context.packageName}-${context.getString(R.string.app_name)}"

            val notificationBuilder = NotificationCompat.Builder(context, channelId).apply {
                setSmallIcon(R.drawable.ic_launcher_foreground)
                setContentTitle(title)
                setContentText(message)
                setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                priority = NotificationCompat.PRIORITY_DEFAULT
                setAutoCancel(autoCancel)

                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                setContentIntent(pendingIntent)
            }
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(cityId, notificationBuilder.build())

        }

        fun createPendingIntent(context: Context, weather: Weather): PendingIntent? {
            val intent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
                action = "Weather notification"//context.getString(R.string.action_notify_administer_medication)
                type = "${weather.placeId}-${weather.cityName}"
                putExtra(
                    "content",
                    "${weather.cityName}: ${weather.weather.temperature.roundToInt()} degrees"
                )
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }


        fun scheduleNotification(context: Context, timeOfNotification: Calendar, data: Weather) {
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra(EXTRA_TITLE, "Weather for ${data.cityName}")
            intent.putExtra(EXTRA_TEXT, "Temperature: ${data.daily[0].temperature.min}/${data.daily[0].temperature.max}}")
            intent.putExtra(EXTRA_ID, data.cityId)
            intent.putExtra(EXTRA_PLACE_ID, data.placeId)
            val pending =
                PendingIntent.getBroadcast(context, data.cityId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            // Schedule notification
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val repeatInterval: Long = 1000 * 60 * 10 // Ten minutes
            manager.setRepeating(AlarmManager.RTC_WAKEUP, timeOfNotification.timeInMillis, repeatInterval, pending)
        }
    }
}