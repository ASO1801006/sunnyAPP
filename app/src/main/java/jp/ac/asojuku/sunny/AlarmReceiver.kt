package jp.ac.asojuku.sunny

import android.app.Notification
import android.app.Notification.Builder
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.core.app.NotificationCompat


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = intent.getIntExtra(NOTIFICATION_ID, 0)
        val content =
            intent.getStringExtra(NOTIFICATION_CONTENT)
        notificationManager.notify(id, buildNotification(context, content))

    }

    private fun buildNotification(context: Context, content: String): Notification {
        var builder = NotificationCompat.Builder(context,content)
            .setContentTitle("My notification")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Much longer text that cannot fit one line..."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        Log.i(javaClass.name,builder.build().toString())
        return builder.build();

    }


    companion object {
        var NOTIFICATION_ID = "notificationId"
        var NOTIFICATION_CONTENT = "content"
    }
}