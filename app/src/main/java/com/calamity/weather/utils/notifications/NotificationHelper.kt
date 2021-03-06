package com.calamity.weather.utils.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.calamity.weather.R
import com.calamity.weather.data.api.openweather.Weather
import com.calamity.weather.ui.mainactivity.MainActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class NotificationHelper {
    companion object {

        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
        const val EXTRA_ID = "cityId"
        const val EXTRA_PLACE_ID = "placeId"
        const val EXTRA_CITY_NAME = "cityName"
        const val EXTRA_INTERVAL = "interval"
        const val EXTRA_NOTIFICATION_TIME = "time"

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
                Log.v("Notifications", "Created channel $channelId")
            }
        }

        fun createNotification(
            context: Context,
            title: String,
            message: String,
            bigText: String,
            channelId: String,
            id: Int,
            autoCancel: Boolean = true
        ) {

            Log.v("Notifications", "Fired notification creation with following parameters: $title, $message, $channelId, $id")
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
            notificationManager.notify(id, notificationBuilder.build())

        }


        fun scheduleNotification(context: Context, timeOfNotification: Calendar, repeatInterval: Long, data: Weather) {
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra(EXTRA_TITLE, "Weather for ${data.cityName}")
            intent.putExtra(EXTRA_TEXT, "Temperature: ${data.daily[0].temperature.min.roundToInt()}°/${data.daily[0].temperature.max.roundToInt()}°")
            intent.putExtra(EXTRA_ID, data.id)
            intent.putExtra(EXTRA_PLACE_ID, data.placeId)
            intent.putExtra(EXTRA_CITY_NAME, data.cityName)
            intent.putExtra(EXTRA_INTERVAL, repeatInterval)
            intent.putExtra(EXTRA_NOTIFICATION_TIME, (timeOfNotification.timeInMillis + repeatInterval))
            val pending =
                PendingIntent.getBroadcast(context, data.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            // Schedule notification
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeOfNotification.timeInMillis, pending)
            Log.v("Notifications", "Scheduled notification id ${data.id} with start at " +
                    "${SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).format(timeOfNotification.timeInMillis)} and interval " +
                    SimpleDateFormat("MM-dd hh:mm:ss", Locale.getDefault()).format(repeatInterval))
        }
    }
}