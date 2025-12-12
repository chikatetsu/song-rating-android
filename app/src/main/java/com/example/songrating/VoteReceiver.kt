package com.example.songrating

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.songrating.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VoteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val betterSong = intent.getStringExtra("betterSong") ?: return
        val worseSong = intent.getStringExtra("worseSong") ?: return

        MusicNotifier.showLoadingNotification(context)

        CoroutineScope(Dispatchers.IO).launch {

            val result = ApiService.sendVote(betterSong, worseSong)

            result.onSuccess { message ->
                MusicNotifier.showSuccessNotification(context, message)
            }

            result.onFailure { err ->
                Log.e("SongRating", "Erreur API", err)
                MusicNotifier.showErrorNotification(context, err.message ?: err.toString())
            }
        }
    }
}
