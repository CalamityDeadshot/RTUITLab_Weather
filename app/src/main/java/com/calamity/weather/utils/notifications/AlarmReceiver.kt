package com.calamity.weather.utils.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.calamity.weather.R
import com.calamity.weather.ui.mainactivity.MainActivity


class AlarmReceiver : BroadcastReceiver() {
    private var mNotificationManager: NotificationManager? = null

    // Notification ID.
    private val NOTIFICATION_ID = 0

    // Notification channel ID.
    private val PRIMARY_CHANNEL_ID = "weather_notification_channel"

    /**
     * Called when the BroadcastReceiver receives an Intent broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Deliver the notification.
        Log.v("Notifications", "Receiver received call")
        if (intent.extras == null) return
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE)!!
        val text = intent.getStringExtra(NotificationHelper.EXTRA_TEXT)!!
        val id = intent.getIntExtra(NotificationHelper.EXTRA_ID, -1)
        val placeId = intent.getStringExtra(NotificationHelper.EXTRA_PLACE_ID)!!
        NotificationHelper.createNotification(
            context,
            title,
            text,
            "",
            placeId,
            id
        )
        //deliverNotification(context, intent)
    }

    /**
     * Builds and delivers the notification.
     *
     * @param context, activity context.
     */
    private fun deliverNotification(context: Context, intent: Intent) {
        // Create the content intent for the notification, which launches
        // this activity
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // Build the notification
        val builder = NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Weather")
            .setContentText(intent.getStringExtra("content"))
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // Deliver the notification
        mNotificationManager!!.notify(NOTIFICATION_ID, builder.build())
    }


}