package com.example.taskmanager

import android.annotation.SuppressLint
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
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        var mediaPlayer: MediaPlayer? = null
    }

    @SuppressLint("LaunchActivityFromNotification")
    override fun onReceive(context: Context, intent: Intent) {

        val taskTitle = intent.getStringExtra("taskTitle") ?: "Task" // Retrieve task title from intent

        // Acquire a wake lock to wake up the device
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "taskmanager:alarmWakeLock"
        )
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)

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

        // Get the duration of the sound (in milliseconds)
        val soundDuration = mediaPlayer?.duration ?: 0

        // Automatically stop the alarm after it rings 10 times
        val stopAlarmAfter = soundDuration * 2L // Total duration after which alarm should stop
        val handler = android.os.Handler()
        handler.postDelayed({
            stopAlarm(context)
        }, stopAlarmAfter)


        // Intent to stop the alarm
        val stopAlarmIntent = Intent(context, StopAlarmReceiver::class.java)
        val stopAlarmPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            stopAlarmIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build and show the notification
        val notification = NotificationCompat.Builder(context, "task_alarm_channel")
            .setContentTitle(taskTitle) // Display the task title here
            .setContentText("Tap to dismiss the alarm") // You can modify this text if needed
            .setSmallIcon(R.drawable.app_logo)  // replace with your icon
            .addAction(R.drawable.app_logo, "Stop Alarm", stopAlarmPendingIntent)  // Add stop action button
            .setDeleteIntent(stopAlarmPendingIntent)  // Stop alarm when notification is swiped away
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(stopAlarmPendingIntent)
            .setAutoCancel(true)
            .setSound(null) // Disable default sound since we're using custom MediaPlayer
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000)) // Example vibration pattern
            .build()

        NotificationManagerCompat.from(context).notify(1, notification)

        // Release the wake lock after a short delay to keep the screen on for a while
        wakeLock.release()
    }

    private fun stopAlarm(context: Context) {
        AlarmReceiver.mediaPlayer?.let { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
            AlarmReceiver.mediaPlayer = null
            Log.d("AlarmReceiver", "Alarm automatically stopped after 10 rings")
        } ?: Log.d("AlarmReceiver", "MediaPlayer was null or not playing")

        // Dismiss the notification as well
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Alarm Channel"
            val descriptionText = "Channel for task alarms"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("task_alarm_channel", name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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

