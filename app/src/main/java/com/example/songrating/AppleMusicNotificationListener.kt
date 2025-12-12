@file:Suppress("MissingPermission")

package com.example.songrating

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class AppleMusicNotificationListener : NotificationListenerService() {
    var lastSong = ""

    /** Show the vote notification if Apple Music is open */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg == "com.apple.android.music") {
            val extras = sbn.notification.extras
            val title = extras.getString("android.title") ?: return
            val artist = extras.getString("android.text") ?: return
            if (title.isEmpty() || artist.isEmpty() || title == "Téléchargement...")
                return

            val currentSong = formatSong(title, artist)
            Log.v("SongRating", currentSong)

            if (lastSong != "" && lastSong != currentSong)
                MusicNotifier.showVoteNotification(this, lastSong, currentSong)

            lastSong = currentSong
        }
    }

    /** If Apple Music is closed, remove the notification */
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg == "com.apple.android.music") {
            MusicNotifier.removeNotification(this)
        }
    }

    /** Use the correct format for the song title */
    fun formatSong(title: String, artist: String): String {
        val formattedTitle = title.replace(" \uD83C\uDD74", "")
        return "$formattedTitle - $artist"
    }
}
