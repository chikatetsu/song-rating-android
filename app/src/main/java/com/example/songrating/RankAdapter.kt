package com.example.songrating

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray

class RankAdapter(private val ranks: JSONArray) : RecyclerView.Adapter<RankAdapter.RankViewHolder>() {

    class RankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val position: TextView = view.findViewById(R.id.rankPosition)
        val name: TextView = view.findViewById(R.id.songName)
        val score: TextView = view.findViewById(R.id.songScore)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RankViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rank, parent, false)

        return RankViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RankViewHolder,
        position: Int
    ) {
        val song = ranks.getJSONObject(position)

        val name = song.getString("name")
        val score = song.getDouble("score")

        holder.position.text = "#${position + 1}"
        holder.name.text = name
        holder.score.text = "Score : %.2f".format(score)
    }

    override fun getItemCount(): Int {
        return ranks.length()
    }
}
