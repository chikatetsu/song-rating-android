package com.example.songrating

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

object MusicNotifier {
    private const val CHANNEL_ID = "song_rating_channel"
    private var manager: NotificationManager? = null
    private const val NOTIFICATION_ID = 1


    private fun getManager(context: Context): NotificationManager {
        if (manager == null) {
            manager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, "Song Rating", NotificationManager.IMPORTANCE_DEFAULT)
            manager!!.createNotificationChannel(channel)
        }
        return manager!!
    }

    fun showVoteNotification(context: Context, lastSong: String, currentSong: String) {
        val manager = getManager(context)

        val upIntent = Intent(context, VoteReceiver::class.java).apply {
            action = "VOTE_UP"
            putExtra("betterSong", currentSong)
            putExtra("worseSong", lastSong)
        }
        val downIntent = Intent(context, VoteReceiver::class.java).apply {
            action = "VOTE_DOWN"
            putExtra("betterSong", lastSong)
            putExtra("worseSong", currentSong)
        }

        val upPending = PendingIntent.getBroadcast(
            context, 0, upIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val downPending = PendingIntent.getBroadcast(
            context, 1, downIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Mieux que la musique précédente?")
            .setContentText(lastSong)
            .addAction(android.R.drawable.arrow_up_float, "👍", upPending)
            .addAction(android.R.drawable.arrow_down_float, "👎", downPending)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    fun showSuccessNotification(context: Context, response: String) {
        val manager = getManager(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Le vote a été pris en compte")
            .setContentText(response)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    fun showErrorNotification(context: Context, error: String) {
        val manager = getManager(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Une erreur s'est produite durant le vote")
            .setContentText(error)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    fun showLoadingNotification(context: Context) {
        val manager = getManager(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("Attente de la réponse du serveur...")
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    fun removeNotification(context: Context) {
        val manager = getManager(context)
        manager.cancel(NOTIFICATION_ID)
    }
}
