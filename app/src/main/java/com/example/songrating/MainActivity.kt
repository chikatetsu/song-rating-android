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

    private data class SearchResult(
        val position: Int,
        val score: Double
    )

    private fun findBestMatch(query: String): Int {
        val normalizedQuery = normalize(query)

        if (normalizedQuery.isEmpty()) {
            return -1
        }

        val queryTokens = normalizedQuery
            .split(" ")
            .filter { it.isNotEmpty() }

        if (queryTokens.isEmpty()) {
            return -1
        }

        var bestResult: SearchResult? = null

        for (i in 0 until ranks.length()) {
            val song = ranks.getJSONObject(i)
            val name = song.getString("name")

            val normalizedName = normalize(name)

            val score = calculateSearchScore(
                normalizedQuery,
                queryTokens,
                normalizedName
            )

            if (score > 0.0 && (bestResult == null || score > bestResult.score)
            ) {
                bestResult = SearchResult(i, score)
            }
        }

        val minimumScore = 35.0

        return if (bestResult != null && bestResult.score >= minimumScore) {
            bestResult.position
        } else {
            -1
        }
    }

    private fun calculateSearchScore(query: String, queryTokens: List<String>, song: String): Double {
        val songTokens = song
            .split(" ")
            .filter { it.isNotEmpty() }

        var score = 0.0

        if (song == query) {
            return 1000.0
        }
        if (song.contains(query)) {
            score += 500.0
        }

        var exactTokenMatches = 0

        for (queryToken in queryTokens) {

            if (songTokens.any { it == queryToken }) {
                exactTokenMatches++
                score += 180.0
            }
        }

        var prefixMatches = 0

        for (queryToken in queryTokens) {
            if (queryToken.length < 3) {
                continue
            }

            if (songTokens.any { it.startsWith(queryToken) || queryToken.startsWith(it) }) {
                prefixMatches++
                score += 90.0
            }
        }

        var fuzzyMatches = 0

        for (queryToken in queryTokens) {
            if (queryToken.length < 3) {
                continue
            }

            var bestTokenSimilarity = 0.0

            for (songToken in songTokens) {
                if (songToken.length < 3) {
                    continue
                }

                val similarity = stringSimilarity(
                    queryToken,
                    songToken
                )

                if (similarity > bestTokenSimilarity) {
                    bestTokenSimilarity = similarity
                }
            }

            if (bestTokenSimilarity >= 0.75) {
                fuzzyMatches++
                score += 70.0 * bestTokenSimilarity
            }
        }

        if (queryTokens.size > 1) {
            val matchedWords = exactTokenMatches + prefixMatches + fuzzyMatches
            if (matchedWords >= queryTokens.size) {
                score += 150.0
            }
        }

        if (queryTokens.size == 1) {
            val token = queryTokens[0]

            if (songTokens.any { it == token }) {
                score += 250.0
            }
        }

        return score
    }

    private fun stringSimilarity(a: String, b: String): Double {
        if (a == b) {
            return 1.0
        }

        val lengthDifference = kotlin.math.abs(a.length - b.length)

        if (lengthDifference > maxOf(2, a.length / 2)) {
            return 0.0
        }

        val distance = levenshteinDistance(a, b)
        val maxLength = maxOf(a.length, b.length)

        if (maxLength == 0) {
            return 1.0
        }

        return 1.0 - distance.toDouble() / maxLength
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        var previousRow = IntArray(b.length + 1) { it }

        for (i in a.indices) {
            val currentRow = IntArray(b.length + 1)
            currentRow[0] = i + 1

            for (j in b.indices) {
                val insertCost = currentRow[j] + 1
                val deleteCost = previousRow[j + 1] + 1
                val replaceCost = previousRow[j] + if (a[i] == b[j]) 0 else 1

                currentRow[j + 1] = minOf(insertCost, deleteCost, replaceCost)
            }

            previousRow = currentRow
        }

        return previousRow[b.length]
    }

    private fun normalize(text: String): String {
        return Normalizer
            .normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .replace("[^\\p{L}\\p{N} ]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}
