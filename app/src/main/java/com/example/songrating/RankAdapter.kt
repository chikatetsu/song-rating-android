package com.example.songrating

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray

class RankAdapter(private val ranks: JSONArray) : RecyclerView.Adapter<RankAdapter.RankViewHolder>() {

    class RankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val position: TextView = view.findViewById(R.id.rankPosition)
        val progression: TextView = view.findViewById(R.id.rankProgression)
        val name: TextView = view.findViewById(R.id.songName)
        val score: TextView = view.findViewById(R.id.songScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_rank, parent, false)

        return RankViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankViewHolder, position: Int) {
        val song = ranks.getJSONObject(position)

        val name = song.getString("name")
        val score = song.getDouble("score")
        val oldRank = song.getInt("old_rank")
        val currentRank = position + 1

        holder.position.text = "#$currentRank"
        holder.name.text = name
        holder.score.text = "Score : %.2f".format(score)

        val progression = oldRank - currentRank
        when {
            progression > 0 -> {
                holder.progression.text = "▲ $progression"
                holder.progression.setTextColor(Color.rgb(46, 125, 50))
            }
            progression < 0 -> {
                holder.progression.text = "▼ ${-progression}"
                holder.progression.setTextColor(Color.rgb(198, 40, 40))
            }
            else -> {
                holder.progression.text = "—"
                holder.progression.setTextColor(Color.rgb(245, 180, 0))
            }
        }
    }

    override fun getItemCount(): Int {
        return ranks.length()
    }
}
