package com.example.taskmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        val uriString = intent.getStringExtra("alarmSoundUri")
        val soundUri = uriString?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        Log.d("AlarmReceiver", "Sound URI: $soundUri")

        // Play sound using MediaPlayer
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, soundUri)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            isLooping = true  // Ensure the alarm keeps ringing until dismissed
            prepare()
            start()
        }

        // Intent to stop the alarm
        val stopAlarmIntent = Intent(context, StopAlarmReceiver::class.java)
        val stopAlarmPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            stopAlarmIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create and show the notification with the stop action
        val notification = NotificationCompat.Builder(context, "channel_id")
            .setContentTitle("Task Reminder")
            .setContentText("Your task is due!")
            .setSmallIcon(R.drawable.app_logo)
            .addAction(R.drawable.app_logo, "Stop Alarm", stopAlarmPendingIntent)  // Add stop action button
            .setDeleteIntent(stopAlarmPendingIntent)  // Stop alarm when notification is swiped away
            .build()

        NotificationManagerCompat.from(context).notify(0, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "channel_id"
            val channelName = "Task Reminder Channel"
            val channelDescription = "Channel for task reminders"

            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}


class StopAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AlarmReceiver.mediaPlayer?.let { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
            AlarmReceiver.mediaPlayer = null
            Log.d("StopAlarmReceiver", "Alarm stopped and media player released")
        } ?: Log.d("StopAlarmReceiver", "MediaPlayer was null or not playing")
    }
}

