package com.calamity.weather.utils.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*


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
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE)!!
        val text = intent.getStringExtra(NotificationHelper.EXTRA_TEXT)!!
        val id = intent.getIntExtra(NotificationHelper.EXTRA_ID, -1)
        val placeId = intent.getStringExtra(NotificationHelper.EXTRA_PLACE_ID)
        val cityName = intent.getStringExtra(NotificationHelper.EXTRA_CITY_NAME)
        val interval = intent.getLongExtra(NotificationHelper.EXTRA_INTERVAL, -1)
        NotificationHelper.createNotification(
            context,
            title,
            text,
            "",
            "$placeId-$cityName",
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
    }


}