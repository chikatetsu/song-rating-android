package com.example.songrating

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.songrating.network.ApiService
import org.json.JSONArray
import java.text.Normalizer
import kotlin.concurrent.thread
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    private lateinit var ranksRecyclerView: RecyclerView
    private lateinit var notificationSettingsButton: Button
    private lateinit var searchEditText: EditText

    private var ranks: JSONArray = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        notificationSettingsButton = findViewById(R.id.notificationSettingsButton)
        ranksRecyclerView = findViewById(R.id.ranksRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        ranksRecyclerView.layoutManager = LinearLayoutManager(this)

        notificationSettingsButton.setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            )
        }

        searchEditText.setOnEditorActionListener { _, _, _ ->
            searchSong()
            true
        }

        loadRanks()
    }

    private fun loadRanks() {
        thread {
            val result = ApiService.getRanks()

            runOnUiThread {
                result.onSuccess { receivedRanks ->
                    ranks = receivedRanks
                    ranksRecyclerView.adapter = RankAdapter(ranks)
                }.onFailure { error ->
                    Toast.makeText(
                        this,
                        "Erreur : ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun searchSong() {
        val query = searchEditText.text.toString().trim()

        if (query.isEmpty()) {
            return
        }

        if (ranks.length() == 0) {
            return
        }

        val position = findBestMatch(query)

        if (position == -1) {
            Toast.makeText(
                this,
                "Aucune musique trouvée",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        ranksRecyclerView.post {
            ranksRecyclerView.scrollToPosition(position)
        }

        val song = ranks.getJSONObject(position)
        val songName = song.getString("name")

        Toast.makeText(
            this,
            "#${position + 1} — $songName",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun findBestMatch(query: String): Int {
        val normalizedQuery = normalize(query)

        var bestPosition = -1
        var bestScore = Double.MAX_VALUE

        for (i in 0 until ranks.length()) {

            val song = ranks.getJSONObject(i)
            val name = song.getString("name")

            val normalizedName = normalize(name)

            // Correspondance exacte
            if (normalizedName == normalizedQuery) {
                return i
            }

            // Si le nom contient directement la recherche, on lui donne une priorité très forte.
            if (normalizedName.contains(normalizedQuery)) {
                val score = normalizedName.length - normalizedQuery.length
                if (score < bestScore) {
                    bestScore = score.toDouble()
                    bestPosition = i
                }
                continue
            }

            // Sinon, recherche par similarité
            val distance = levenshteinDistance(normalizedQuery, normalizedName)

            if (distance < bestScore) {
                bestScore = distance.toDouble()
                bestPosition = i
            }
        }

        return bestPosition
    }

    private fun normalize(text: String): String {
        return Normalizer
            .normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .replace("[^a-z0-9 ]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val previousRow = IntArray(b.length + 1) { it }

        for (i in a.indices) {
            val currentRow = IntArray(b.length + 1)
            currentRow[0] = i + 1

            for (j in b.indices) {
                val insertCost = currentRow[j] + 1
                val deleteCost = previousRow[j + 1] + 1
                val replaceCost = previousRow[j] + if (a[i] == b[j]) 0 else 1
                currentRow[j + 1] = min(insertCost,min(deleteCost, replaceCost))
            }

            for (j in currentRow.indices) {
                previousRow[j] = currentRow[j]
            }
        }

        return previousRow[b.length]
    }
}
